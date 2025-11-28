package com.rollup.rollupapi.common.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRoomRequest {

    @NotBlank(message = "게임 타입은 필수입니다")
    private String gameType;

    @Min(value = 1, message = "최대 인원은 1명 이상이어야 합니다")
    @Max(value = 10, message = "최대 인원은 10명 이하여야 합니다")
    private int maxPlayers;

    private String roomName;
}
