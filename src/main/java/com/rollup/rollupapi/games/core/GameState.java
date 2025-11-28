package com.rollup.rollupapi.games.core;

import java.util.List;
import java.util.Map;

/**
 * 게임 상태 추상 클래스
 * 각 게임은 이 클래스를 상속받아 구현
 */
public abstract class GameState {

    /**
     * 현재 턴인 플레이어 ID
     */
    public abstract String getCurrentPlayerId();

    /**
     * 플레이어 목록
     */
    public abstract List<Player> getPlayers();

    /**
     * 게임 상태를 Map으로 직렬화 (Firestore 저장용)
     */
    public abstract Map<String, Object> toMap();

    /**
     * 상태 복사 (불변성 유지를 위해)
     */
    public abstract GameState copy();
}
