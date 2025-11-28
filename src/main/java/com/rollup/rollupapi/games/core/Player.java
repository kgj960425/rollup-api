package com.rollup.rollupapi.games.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 플레이어 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {
    private String id;
    private String nickname;
}
