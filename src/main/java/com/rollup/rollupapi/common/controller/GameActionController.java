package com.rollup.rollupapi.common.controller;

import com.rollup.rollupapi.common.dto.request.GameActionRequest;
import com.rollup.rollupapi.common.dto.response.ApiResponse;
import com.rollup.rollupapi.common.dto.response.GameStateResponse;
import com.rollup.rollupapi.common.service.GameDispatcherService;
import com.rollup.rollupapi.security.FirebaseUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 게임 액션 API (통합 엔드포인트)
 * POST /api/game/{roomId}/action - 모든 게임 액션 처리
 * GET /api/game/{roomId}/state - 현재 게임 상태 조회
 */
@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameActionController {

    private final GameDispatcherService gameDispatcherService;

    /**
     * 게임 액션 처리
     *
     * 야추 다이스 예시:
     * - 주사위 굴리기: {"actionType": "ROLL_DICE", "payload": {"heldDice": [false, true, true, false, false]}}
     * - 점수 기록: {"actionType": "SCORE", "payload": {"category": "fullHouse"}}
     *
     * 렉시오 예시:
     * - 카드 내기: {"actionType": "PLAY_CARDS", "payload": {"cardIds": ["card1", "card2"]}}
     * - 패스: {"actionType": "PASS", "payload": {}}
     */
    @PostMapping("/{roomId}/action")
    public ApiResponse<GameStateResponse> handleAction(
            @AuthenticationPrincipal FirebaseUserPrincipal principal,
            @PathVariable String roomId,
            @Valid @RequestBody GameActionRequest request) {
        GameStateResponse state = gameDispatcherService.handleAction(roomId, principal.getUid(), request);
        return ApiResponse.success(state);
    }

    @GetMapping("/{roomId}/state")
    public ApiResponse<GameStateResponse> getGameState(
            @AuthenticationPrincipal FirebaseUserPrincipal principal,
            @PathVariable String roomId) {
        GameStateResponse state = gameDispatcherService.getGameState(roomId, principal.getUid());
        return ApiResponse.success(state);
    }
}
