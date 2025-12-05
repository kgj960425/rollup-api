package com.rollup.rollupapi.common.controller;

import com.rollup.rollupapi.common.dto.response.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthTestController {

    /**
     * 인증 없이 호출 가능 (permitAll)
     */
    @GetMapping("/public")
    public ApiResponse<Map<String, String>> publicEndpoint() {
        return ApiResponse.success(Map.of(
                "message", "This is a public endpoint",
                "auth", "not required"
        ));
    }

    /**
     * 인증 필요 (Firebase Token 검증)
     */
    @GetMapping("/protected")
    public ApiResponse<Map<String, Object>> protectedEndpoint(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ApiResponse.success(Map.of(
                    "message", "Token verified but no user details",
                    "authenticated", true
            ));
        }
        
        return ApiResponse.success(Map.of(
                "message", "You are authenticated!",
                "uid", userDetails.getUsername(),
                "authenticated", true
        ));
    }
}
