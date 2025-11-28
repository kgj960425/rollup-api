package com.rollup.rollupapi.games.core;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 게임 엔진 등록/조회
 * 모든 GameEngine 구현체가 자동으로 등록됨
 */
@Component
public class GameRegistry {

    private final Map<String, GameEngine> engines = new HashMap<>();

    public GameRegistry(List<GameEngine> engineList) {
        for (GameEngine engine : engineList) {
            engines.put(engine.getGameType(), engine);
        }
    }

    public GameEngine getEngine(String gameType) {
        GameEngine engine = engines.get(gameType);
        if (engine == null) {
            throw new IllegalArgumentException("Unknown game type: " + gameType);
        }
        return engine;
    }

    public List<String> getAvailableGames() {
        return List.copyOf(engines.keySet());
    }

    public boolean isSupported(String gameType) {
        return engines.containsKey(gameType);
    }
}
