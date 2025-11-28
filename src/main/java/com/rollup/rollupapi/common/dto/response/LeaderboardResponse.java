package com.rollup.rollupapi.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardResponse {
    private String gameType;
    private List<LeaderboardEntry> entries;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LeaderboardEntry {
        private int rank;
        private String nickname;
        private int gamesPlayed;
        private int gamesWon;
        private double winRate;
        private Integer bestScore;
    }
}
