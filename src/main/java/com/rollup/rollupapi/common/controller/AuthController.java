package com.rollup.rollupapi.common.controller;

import com.rollup.rollupapi.common.dto.request.RegisterRequest;
import com.rollup.rollupapi.common.dto.response.ApiResponse;
import com.rollup.rollupapi.common.dto.response.UserResponse;
import com.rollup.rollupapi.common.service.UserService;
import com.rollup.rollupapi.security.FirebaseUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API
 * POST /api/auth/register - 회원가입 (Firebase 인증 후 DB 등록)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(
            @AuthenticationPrincipal FirebaseUserPrincipal principal,
            @Valid @RequestBody RegisterRequest request) {
        UserResponse user = userService.registerUser(principal.getUid(), request);
        return ApiResponse.success("회원가입 완료", user);
    }
}
