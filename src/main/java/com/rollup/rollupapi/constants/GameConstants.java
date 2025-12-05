package com.rollup.rollupapi.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 게임 목록 상수 정의
 */
public class GameConstants {

    @Getter
    @AllArgsConstructor
    public enum Game {
        YACHT_DICE(
                "yacht_dice",
                "Yacht Dice",
                "주사위를 굴려 점수를 얻는 전략 게임",
                2, 8, 20, true, 1
        ),
        LEXIO(
                "lexio",
                "Lexio",
                "타일을 조합하여 족보를 만드는 전략 게임",
                2, 5, 30, false, 2
        ),
        EXPLODING_KITTENS(
                "exploding_kittens",
                "Exploding Kittens",
                "폭발하는 고양이를 피하는 카드 게임",
                2, 5, 15, false, 3
        ),
        SEVEN_WONDERS_DUEL(
                "seven_wonders_duel",
                "7 Wonders Duel",
                "고대 문명을 건설하는 2인 전략 게임",
                2, 2, 30, false, 4
        ),
        SPLENDOR_DUEL(
                "splendor_duel",
                "Splendor Duel",
                "보석을 수집하여 승리하는 2인 전략 게임",
                2, 2, 30, false, 5
        );

        private final String id;
        private final String displayName;
        private final String description;
        private final int minPlayers;
        private final int maxPlayers;
        private final int estimatedTime;
        private final boolean enabled;
        private final int sortOrder;

        /**
         * ID로 게임 찾기
         */
        public static Game findById(String id) {
            return Arrays.stream(values())
                    .filter(g -> g.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        /**
         * 활성화된 게임 목록
         */
        public static List<Game> getEnabledGames() {
            return Arrays.stream(values())
                    .filter(Game::isEnabled)
                    .sorted((a, b) -> a.getSortOrder() - b.getSortOrder())
                    .collect(Collectors.toList());
        }

        /**
         * 전체 게임 목록 (정렬순)
         */
        public static List<Game> getAllSorted() {
            return Arrays.stream(values())
                    .sorted((a, b) -> a.getSortOrder() - b.getSortOrder())
                    .collect(Collectors.toList());
        }

        /**
         * Map 형태로 변환 (API 응답용)
         */
        public Map<String, Object> toMap() {
            return Map.of(
                    "id", id,
                    "displayName", displayName,
                    "description", description,
                    "minPlayers", minPlayers,
                    "maxPlayers", maxPlayers,
                    "estimatedTime", estimatedTime,
                    "enabled", enabled,
                    "sortOrder", sortOrder
            );
        }
    }
}