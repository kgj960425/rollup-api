package com.rollup.rollupapi.games.core;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 게임 엔진 인터페이스
 * 모든 게임은 이 인터페이스를 구현해야 함
 */
public interface GameEngine {

    /**
     * 게임 타입 반환 (예: "yacht", "lexio")
     */
    String getGameType();

    /**
     * 최소 플레이어 수
     */
    int getMinPlayers();

    /**
     * 최대 플레이어 수
     */
    int getMaxPlayers();

    /**
     * 게임 초기화
     * @param players 플레이어 목록
     * @param seed 랜덤 시드 (재현 가능한 게임을 위해)
     * @return 초기 게임 상태
     */
    GameState initGame(List<Player> players, long seed);

    /**
     * 액션 검증
     * @param state 현재 게임 상태
     * @param playerId 액션을 수행하는 플레이어 ID
     * @param action 수행할 액션
     * @return 검증 결과
     */
    ValidationResult validateAction(GameState state, String playerId, GameAction action);

    /**
     * 액션 적용
     * @param state 현재 게임 상태
     * @param playerId 액션을 수행하는 플레이어 ID
     * @param action 수행할 액션
     * @return 새로운 게임 상태
     */
    GameState applyAction(GameState state, String playerId, GameAction action);

    /**
     * 게임 종료 체크
     * @param state 현재 게임 상태
     * @return 게임이 끝났으면 결과, 아니면 empty
     */
    Optional<GameResultData> checkGameEnd(GameState state);

    /**
     * 공개 상태 반환 (모든 플레이어가 볼 수 있는 정보)
     * @param state 게임 상태
     * @return Firestore에 저장할 공개 상태
     */
    Map<String, Object> getPublicState(GameState state);

    /**
     * 비공개 상태 반환 (특정 플레이어만 볼 수 있는 정보)
     * @param state 게임 상태
     * @param playerId 플레이어 ID
     * @return Firestore에 저장할 비공개 상태
     */
    Map<String, Object> getPrivateState(GameState state, String playerId);

    /**
     * JSON payload를 GameAction으로 변환
     * @param actionType 액션 타입 문자열
     * @param payload JSON payload
     * @return 파싱된 GameAction
     */
    GameAction parseAction(String actionType, Map<String, Object> payload);

    /**
     * Map에서 GameState 복원 (Firestore에서 로드 시 사용)
     */
    GameState deserializeState(Map<String, Object> data);
}
