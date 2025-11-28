package com.rollup.rollupapi.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {
    private String roomId;
    private String gameType;
    private String hostId;
    private String roomName;
    private int maxPlayers;
    private String status;  // waiting, playing, finished
    private List<PlayerInfo> players;
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlayerInfo {
        private String id;
        private String nickname;
        private boolean isReady;
        private boolean isConnected;
    }
}
