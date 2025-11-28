package com.rollup.rollupapi.common.controller;

import com.rollup.rollupapi.common.dto.response.ApiResponse;
import com.rollup.rollupapi.common.dto.response.UserResponse;
import com.rollup.rollupapi.common.dto.response.UserStatsResponse;
import com.rollup.rollupapi.common.service.UserService;
import com.rollup.rollupapi.security.FirebaseUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 유저 관련 API
 * GET /api/users/me - 내 정보
 * GET /api/users/me/stats - 전체 게임 통계
 * GET /api/users/me/stats/{gameType} - 특정 게임 통계
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMyInfo(
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        UserResponse user = userService.getUser(principal.getUid());
        return ApiResponse.success(user);
    }

    @GetMapping("/me/stats")
    public ApiResponse<List<UserStatsResponse>> getMyStats(
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        List<UserStatsResponse> stats = userService.getUserStats(principal.getUid());
        return ApiResponse.success(stats);
    }

    @GetMapping("/me/stats/{gameType}")
    public ApiResponse<UserStatsResponse> getMyStatsByGameType(
            @AuthenticationPrincipal FirebaseUserPrincipal principal,
            @PathVariable String gameType) {
        UserStatsResponse stats = userService.getUserStatsByGameType(principal.getUid(), gameType);
        return ApiResponse.success(stats);
    }
}
