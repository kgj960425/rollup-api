package com.rollup.rollupapi.common.service;

import com.rollup.rollupapi.common.dto.response.LeaderboardResponse;
import com.rollup.rollupapi.common.entity.UserGameStats;
import com.rollup.rollupapi.common.repository.UserGameStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final UserGameStatsRepository userGameStatsRepository;

    @Transactional(readOnly = true)
    public LeaderboardResponse getLeaderboard(String gameType) {
        List<UserGameStats> stats = userGameStatsRepository.findTopByGameTypeOrderByWinRate(gameType);

        List<LeaderboardResponse.LeaderboardEntry> entries = new ArrayList<>();
        int rank = 1;

        for (UserGameStats stat : stats) {
            if (stat.getGamesPlayed() == 0) continue;

            double winRate = (double) stat.getGamesWon() / stat.getGamesPlayed() * 100;
            winRate = Math.round(winRate * 100.0) / 100.0;

            entries.add(LeaderboardResponse.LeaderboardEntry.builder()
                    .rank(rank++)
                    .nickname(stat.getUser().getNickname())
                    .gamesPlayed(stat.getGamesPlayed())
                    .gamesWon(stat.getGamesWon())
                    .winRate(winRate)
                    .bestScore(stat.getBestScore())
                    .build());

            if (rank > 100) break;  // 상위 100명까지만
        }

        return LeaderboardResponse.builder()
                .gameType(gameType)
                .entries(entries)
                .build();
    }
}
