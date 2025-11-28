package com.rollup.rollupapi.common.controller;

import com.rollup.rollupapi.common.dto.request.CreateRoomRequest;
import com.rollup.rollupapi.common.dto.response.ApiResponse;
import com.rollup.rollupapi.common.dto.response.RoomResponse;
import com.rollup.rollupapi.common.service.RoomService;
import com.rollup.rollupapi.common.service.UserService;
import com.rollup.rollupapi.security.FirebaseUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 방(로비/대기실) 관련 API
 * GET /api/rooms?gameType={gameType} - 게임별 대기 방 목록
 * POST /api/rooms - 방 생성
 * POST /api/rooms/{roomId}/join - 방 입장
 * POST /api/rooms/{roomId}/leave - 방 퇴장
 * POST /api/rooms/{roomId}/ready - 준비 토글
 * POST /api/rooms/{roomId}/start - 게임 시작
 * POST /api/rooms/{roomId}/kick/{userId} - 강퇴
 */
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final UserService userService;

    @GetMapping
    public ApiResponse<List<RoomResponse>> getRooms(@RequestParam String gameType) {
        List<RoomResponse> rooms = roomService.getRoomsByGameType(gameType);
        return ApiResponse.success(rooms);
    }

    @GetMapping("/{roomId}")
    public ApiResponse<RoomResponse> getRoomInfo(@PathVariable String roomId) {
        RoomResponse room = roomService.getRoomInfo(roomId);
        return ApiResponse.success(room);
    }

    @PostMapping
    public ApiResponse<RoomResponse> createRoom(
            @AuthenticationPrincipal FirebaseUserPrincipal principal,
            @Valid @RequestBody CreateRoomRequest request) {
        String nickname = getNickname(principal);
        RoomResponse room = roomService.createRoom(principal.getUid(), nickname, request);
        return ApiResponse.success("방이 생성되었습니다", room);
    }

    @PostMapping("/{roomId}/join")
    public ApiResponse<RoomResponse> joinRoom(
            @AuthenticationPrincipal FirebaseUserPrincipal principal,
            @PathVariable String roomId) {
        String nickname = getNickname(principal);
        RoomResponse room = roomService.joinRoom(roomId, principal.getUid(), nickname);
        return ApiResponse.success("방에 입장했습니다", room);
    }

    @PostMapping("/{roomId}/leave")
    public ApiResponse<Void> leaveRoom(
            @AuthenticationPrincipal FirebaseUserPrincipal principal,
            @PathVariable String roomId) {
        roomService.leaveRoom(roomId, principal.getUid());
        return ApiResponse.success("방을 나갔습니다", null);
    }

    @PostMapping("/{roomId}/ready")
    public ApiResponse<RoomResponse> toggleReady(
            @AuthenticationPrincipal FirebaseUserPrincipal principal,
            @PathVariable String roomId) {
        RoomResponse room = roomService.toggleReady(roomId, principal.getUid());
        return ApiResponse.success(room);
    }

    @PostMapping("/{roomId}/start")
    public ApiResponse<RoomResponse> startGame(
            @AuthenticationPrincipal FirebaseUserPrincipal principal,
            @PathVariable String roomId) {
        RoomResponse room = roomService.startGame(roomId, principal.getUid());
        return ApiResponse.success("게임이 시작되었습니다", room);
    }

    @PostMapping("/{roomId}/kick/{userId}")
    public ApiResponse<Void> kickPlayer(
            @AuthenticationPrincipal FirebaseUserPrincipal principal,
            @PathVariable String roomId,
            @PathVariable String userId) {
        roomService.kickPlayer(roomId, principal.getUid(), userId);
        return ApiResponse.success("플레이어를 강퇴했습니다", null);
    }

    private String getNickname(FirebaseUserPrincipal principal) {
        try {
            return userService.getUser(principal.getUid()).getNickname();
        } catch (Exception e) {
            return principal.getDisplayName() != null ? principal.getDisplayName() : "Player";
        }
    }
}
