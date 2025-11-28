package com.rollup.rollupapi.games.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 게임 결과 데이터
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameResultData {
    private String winnerId;
    private List<PlayerResult> playerResults;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlayerResult {
        private String playerId;
        private String nickname;
        private int score;
        private int rank;
    }
}
