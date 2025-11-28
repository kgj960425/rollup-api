package com.rollup.rollupapi.common.service;

import com.rollup.rollupapi.common.dto.request.CreateRoomRequest;
import com.rollup.rollupapi.common.dto.response.RoomResponse;
import com.rollup.rollupapi.common.exception.BadRequestException;
import com.rollup.rollupapi.common.exception.ResourceNotFoundException;
import com.rollup.rollupapi.games.core.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final FirestoreService firestoreService;
    private final GameMetaService gameMetaService;
    private final GameRegistry gameRegistry;
    private final UserService userService;

    public RoomResponse createRoom(String userId, String userNickname, CreateRoomRequest request) {
        // 게임 타입 유효성 검사
        if (!gameMetaService.isValidGameType(request.getGameType())) {
            throw new BadRequestException("유효하지 않은 게임 타입입니다: " + request.getGameType());
        }

        // 게임 엔진에서 최대 인원 검증
        GameEngine engine = gameRegistry.getEngine(request.getGameType());
        if (request.getMaxPlayers() < engine.getMinPlayers() || request.getMaxPlayers() > engine.getMaxPlayers()) {
            throw new BadRequestException(String.format("인원 수는 %d ~ %d명이어야 합니다",
                    engine.getMinPlayers(), engine.getMaxPlayers()));
        }

        String roomId = firestoreService.createRoom(
                request.getGameType(),
                userId,
                userNickname,
                request.getMaxPlayers(),
                request.getRoomName()
        );

        return firestoreService.getRoomInfo(roomId);
    }

    public List<RoomResponse> getRoomsByGameType(String gameType) {
        return firestoreService.getRoomsByGameType(gameType);
    }

    public RoomResponse getRoomInfo(String roomId) {
        RoomResponse room = firestoreService.getRoomInfo(roomId);
        if (room == null) {
            throw new ResourceNotFoundException("방을 찾을 수 없습니다");
        }
        return room;
    }

    public RoomResponse joinRoom(String roomId, String userId, String nickname) {
        firestoreService.joinRoom(roomId, userId, nickname);
        return firestoreService.getRoomInfo(roomId);
    }

    public void leaveRoom(String roomId, String userId) {
        firestoreService.leaveRoom(roomId, userId);
    }

    public RoomResponse toggleReady(String roomId, String userId) {
        firestoreService.toggleReady(roomId, userId);
        return firestoreService.getRoomInfo(roomId);
    }

    public RoomResponse startGame(String roomId, String userId) {
        RoomResponse room = getRoomInfo(roomId);

        // 방장인지 확인
        if (!room.getHostId().equals(userId)) {
            throw new BadRequestException("방장만 게임을 시작할 수 있습니다");
        }

        // 이미 게임 중인지 확인
        if (!"waiting".equals(room.getStatus())) {
            throw new BadRequestException("이미 게임이 시작되었습니다");
        }

        // 최소 인원 확인
        GameEngine engine = gameRegistry.getEngine(room.getGameType());
        if (room.getPlayers().size() < engine.getMinPlayers()) {
            throw new BadRequestException(String.format("최소 %d명이 필요합니다", engine.getMinPlayers()));
        }

        // 모든 플레이어 준비 완료 확인 (방장 제외)
        for (RoomResponse.PlayerInfo player : room.getPlayers()) {
            if (!player.getId().equals(room.getHostId()) && !player.isReady()) {
                throw new BadRequestException("모든 플레이어가 준비를 완료해야 합니다");
            }
        }

        // 플레이어 목록 생성
        List<Player> players = room.getPlayers().stream()
                .map(p -> Player.builder()
                        .id(p.getId())
                        .nickname(p.getNickname())
                        .build())
                .toList();

        // 게임 초기화
        long seed = System.currentTimeMillis();
        GameState initialState = engine.initGame(players, seed);

        // Firestore에 게임 상태 저장
        Map<String, Object> publicState = engine.getPublicState(initialState);
        firestoreService.saveGameState(roomId, publicState);

        // 비공개 상태 저장 (해당 게임에 있다면)
        for (Player player : players) {
            Map<String, Object> privateState = engine.getPrivateState(initialState, player.getId());
            if (!privateState.isEmpty()) {
                firestoreService.savePrivateState(roomId, player.getId(), privateState);
            }
        }

        // 방 상태를 playing으로 변경
        firestoreService.updateRoomStatus(roomId, "playing");

        return firestoreService.getRoomInfo(roomId);
    }

    public void kickPlayer(String roomId, String hostId, String targetUserId) {
        RoomResponse room = getRoomInfo(roomId);

        if (!room.getHostId().equals(hostId)) {
            throw new BadRequestException("방장만 강퇴할 수 있습니다");
        }

        if (hostId.equals(targetUserId)) {
            throw new BadRequestException("자기 자신을 강퇴할 수 없습니다");
        }

        firestoreService.leaveRoom(roomId, targetUserId);
    }
}
