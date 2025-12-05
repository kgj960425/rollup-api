package com.rollup.rollupapi.games.yacht;

import com.rollup.rollupapi.games.core.GameAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 야추 게임 액션
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YachtAction implements GameAction {
    private YachtActionType type;
    private boolean[] heldDice;  // ROLL_DICE 액션용 (true면 홀드, false면 다시 굴림)
    private String category;     // SCORE 액션용 (점수 기록할 카테고리)

    @Override
    public String getType() {
        return type.name();
    }

    public YachtActionType getActionType() {
        return type;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type.name());
        if (heldDice != null) {
            map.put("heldDice", heldDice);
        }
        if (category != null) {
            map.put("category", category);
        }
        return map;
    }

    public static YachtAction rollDice(boolean[] heldDice) {
        return YachtAction.builder()
                .type(YachtActionType.ROLL_DICE)
                .heldDice(heldDice != null ? heldDice : new boolean[5])
                .build();
    }

    public static YachtAction score(String category) {
        return YachtAction.builder()
                .type(YachtActionType.SCORE)
                .category(category)
                .build();
    }
}
