package com.rollup.rollupapi.common.dto.response;

import com.rollup.rollupapi.common.entity.UserGameStats;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatsResponse {
    private String gameType;
    private int gamesPlayed;
    private int gamesWon;
    private long totalScore;
    private Integer bestScore;
    private double winRate;

    public static UserStatsResponse from(UserGameStats stats) {
        double winRate = stats.getGamesPlayed() > 0
                ? (double) stats.getGamesWon() / stats.getGamesPlayed() * 100
                : 0.0;

        return UserStatsResponse.builder()
                .gameType(stats.getGameType())
                .gamesPlayed(stats.getGamesPlayed())
                .gamesWon(stats.getGamesWon())
                .totalScore(stats.getTotalScore())
                .bestScore(stats.getBestScore())
                .winRate(Math.round(winRate * 100.0) / 100.0)
                .build();
    }
}
