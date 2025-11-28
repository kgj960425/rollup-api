package com.rollup.rollupapi.common.service;

import com.rollup.rollupapi.common.dto.request.GameActionRequest;
import com.rollup.rollupapi.common.dto.response.GameStateResponse;
import com.rollup.rollupapi.common.dto.response.RoomResponse;
import com.rollup.rollupapi.common.entity.User;
import com.rollup.rollupapi.common.exception.BadRequestException;
import com.rollup.rollupapi.games.core.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameDispatcherService {

    private final GameRegistry gameRegistry;
    private final FirestoreService firestoreService;
    private final LogService logService;
    private final UserService userService;

    public GameStateResponse handleAction(String roomId, String userId, GameActionRequest request) {
        // 1. 방 정보 조회
        RoomResponse room = firestoreService.getRoomInfo(roomId);
        if (room == null) {
            throw new BadRequestException("방을 찾을 수 없습니다");
        }

        if (!"playing".equals(room.getStatus())) {
            throw new BadRequestException("게임이 진행 중이 아닙니다");
        }

        String gameType = room.getGameType();

        // 2. 해당 게임 엔진 가져오기
        GameEngine engine = gameRegistry.getEngine(gameType);

        // 3. 현재 상태 조회
        Map<String, Object> stateData = firestoreService.getGameState(roomId);
        if (stateData == null) {
            throw new BadRequestException("게임 상태를 찾을 수 없습니다");
        }
        GameState currentState = engine.deserializeState(stateData);

        // 4. 액션 파싱
        GameAction action = engine.parseAction(request.getActionType(), request.getPayload());

        // 5. 검증
        ValidationResult validation = engine.validateAction(currentState, userId, action);
        if (!validation.isValid()) {
            throw new BadRequestException(validation.getMessage());
        }

        // 6. 액션 적용
        GameState newState = engine.applyAction(currentState, userId, action);

        // 7. Firestore 업데이트
        Map<String, Object> newPublicState = engine.getPublicState(newState);
        firestoreService.saveGameState(roomId, newPublicState);

        // 비공개 상태 업데이트 (해당 게임에 있다면)
        for (RoomResponse.PlayerInfo player : room.getPlayers()) {
            Map<String, Object> privateState = engine.getPrivateState(newState, player.getId());
            if (!privateState.isEmpty()) {
                firestoreService.savePrivateState(roomId, player.getId(), privateState);
            }
        }

        // 8. 종료 체크
        Optional<GameResultData> result = engine.checkGameEnd(newState);
        if (result.isPresent()) {
            handleGameEnd(roomId, room, result.get());
        }

        // 9. 로그 저장 (비동기)
        logService.saveGameLog(roomId, gameType, userId, request.getActionType(), request.getPayload());

        // 10. 응답 생성
        Map<String, Object> privateState = engine.getPrivateState(newState, userId);

        return GameStateResponse.builder()
                .roomId(roomId)
                .gameType(gameType)
                .status(room.getStatus())
                .publicState(newPublicState)
                .privateState(privateState.isEmpty() ? null : privateState)
                .build();
    }

    public GameStateResponse getGameState(String roomId, String userId) {
        RoomResponse room = firestoreService.getRoomInfo(roomId);
        if (room == null) {
            throw new BadRequestException("방을 찾을 수 없습니다");
        }

        Map<String, Object> publicState = firestoreService.getGameState(roomId);
        Map<String, Object> privateState = firestoreService.getPrivateState(roomId, userId);

        return GameStateResponse.builder()
                .roomId(roomId)
                .gameType(room.getGameType())
                .status(room.getStatus())
                .publicState(publicState)
                .privateState(privateState)
                .build();
    }

    private void handleGameEnd(String roomId, RoomResponse room, GameResultData result) {
        // 방 상태를 finished로 변경
        firestoreService.updateRoomStatus(roomId, "finished");

        // 각 플레이어 통계 업데이트
        for (GameResultData.PlayerResult playerResult : result.getPlayerResults()) {
            boolean isWinner = playerResult.getPlayerId().equals(result.getWinnerId());
            try {
                userService.updateUserStats(
                        playerResult.getPlayerId(),
                        room.getGameType(),
                        playerResult.getScore(),
                        isWinner
                );
            } catch (Exception e) {
                log.warn("Failed to update stats for player: {}", playerResult.getPlayerId(), e);
            }
        }

        // 게임 결과 로그 저장
        logService.saveGameResult(roomId, room.getGameType(), result);
    }
}
