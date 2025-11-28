package com.rollup.rollupapi.common.controller;

import com.rollup.rollupapi.common.dto.response.ApiResponse;
import com.rollup.rollupapi.common.dto.response.GameTypeResponse;
import com.rollup.rollupapi.common.service.GameMetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 게임 메타 정보 API
 * GET /api/games - 활성화된 게임 목록
 * GET /api/games/{gameType} - 게임 상세 정보
 */
@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameMetaController {

    private final GameMetaService gameMetaService;

    @GetMapping
    public ApiResponse<List<GameTypeResponse>> getActiveGames() {
        List<GameTypeResponse> games = gameMetaService.getActiveGames();
        return ApiResponse.success(games);
    }

    @GetMapping("/{gameType}")
    public ApiResponse<GameTypeResponse> getGameByType(@PathVariable String gameType) {
        GameTypeResponse game = gameMetaService.getGameByType(gameType);
        return ApiResponse.success(game);
    }
}
