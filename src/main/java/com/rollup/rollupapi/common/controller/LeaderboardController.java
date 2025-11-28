package com.rollup.rollupapi.common.controller;

import com.rollup.rollupapi.common.dto.response.ApiResponse;
import com.rollup.rollupapi.common.dto.response.LeaderboardResponse;
import com.rollup.rollupapi.common.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 리더보드(랭킹) API
 * GET /api/leaderboard/{gameType} - 게임별 랭킹
 */
@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/{gameType}")
    public ApiResponse<LeaderboardResponse> getLeaderboard(@PathVariable String gameType) {
        LeaderboardResponse leaderboard = leaderboardService.getLeaderboard(gameType);
        return ApiResponse.success(leaderboard);
    }
}
