package com.rollup.rollupapi.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameStateResponse {
    private String roomId;
    private String gameType;
    private String status;
    private Map<String, Object> publicState;
    private Map<String, Object> privateState;  // 요청한 유저의 비공개 상태
}
