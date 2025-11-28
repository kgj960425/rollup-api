package com.rollup.rollupapi.games.core;

import java.util.Map;

/**
 * 게임 액션 인터페이스
 * 각 게임에서 가능한 액션을 정의
 */
public interface GameAction {

    /**
     * 액션 타입 반환
     */
    String getType();

    /**
     * 액션을 Map으로 직렬화 (로깅용)
     */
    Map<String, Object> toMap();
}
