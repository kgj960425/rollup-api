package com.rollup.rollupapi.common.dto.response;

import com.rollup.rollupapi.common.entity.GameType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameTypeResponse {
    private String id;
    private String name;
    private String description;
    private int minPlayers;
    private int maxPlayers;
    private boolean isActive;

    public static GameTypeResponse from(GameType gameType) {
        return GameTypeResponse.builder()
                .id(gameType.getId())
                .name(gameType.getName())
                .description(gameType.getDescription())
                .minPlayers(gameType.getMinPlayers())
                .maxPlayers(gameType.getMaxPlayers())
                .isActive(gameType.isActive())
                .build();
    }
}
