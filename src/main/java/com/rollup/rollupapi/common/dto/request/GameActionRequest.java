package com.rollup.rollupapi.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameActionRequest {

    @NotBlank(message = "액션 타입은 필수입니다")
    private String actionType;

    private Map<String, Object> payload;
}
