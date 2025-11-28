package com.rollup.rollupapi.games.yacht;

import com.rollup.rollupapi.games.core.GameState;
import com.rollup.rollupapi.games.core.Player;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * 야추 게임 상태
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YachtState extends GameState {
    private List<Player> players;
    private int currentPlayerIndex;
    private int round;
    private int[] diceValues;
    private boolean[] heldDice;
    private int rollCount;
    private Map<String, YachtScoreboard> scoreboards;
    private long seed;
    private transient Random random;

    @Override
    public String getCurrentPlayerId() {
        return players.get(currentPlayerIndex).getId();
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("currentPlayerId", getCurrentPlayerId());
        map.put("currentPlayerIndex", currentPlayerIndex);
        map.put("round", round);
        map.put("diceValues", diceValues);
        map.put("heldDice", heldDice);
        map.put("rollCount", rollCount);

        // 플레이어 목록
        List<Map<String, Object>> playerList = new ArrayList<>();
        for (Player p : players) {
            Map<String, Object> playerMap = new HashMap<>();
            playerMap.put("id", p.getId());
            playerMap.put("nickname", p.getNickname());
            playerList.add(playerMap);
        }
        map.put("players", playerList);

        // 점수판
        Map<String, Object> scoreboardsMap = new HashMap<>();
        for (Map.Entry<String, YachtScoreboard> entry : scoreboards.entrySet()) {
            scoreboardsMap.put(entry.getKey(), entry.getValue().toMap());
        }
        map.put("scoreboards", scoreboardsMap);

        return map;
    }

    @Override
    public GameState copy() {
        YachtState copy = new YachtState();
        copy.players = new ArrayList<>(this.players);
        copy.currentPlayerIndex = this.currentPlayerIndex;
        copy.round = this.round;
        copy.diceValues = Arrays.copyOf(this.diceValues, 5);
        copy.heldDice = Arrays.copyOf(this.heldDice, 5);
        copy.rollCount = this.rollCount;
        copy.seed = this.seed;
        copy.random = this.random;

        copy.scoreboards = new HashMap<>();
        for (Map.Entry<String, YachtScoreboard> entry : this.scoreboards.entrySet()) {
            copy.scoreboards.put(entry.getKey(), entry.getValue().copy());
        }

        return copy;
    }

    @SuppressWarnings("unchecked")
    public static YachtState fromMap(Map<String, Object> map) {
        YachtState state = new YachtState();

        // 플레이어 복원
        List<Map<String, Object>> playerList = (List<Map<String, Object>>) map.get("players");
        state.players = new ArrayList<>();
        for (Map<String, Object> pMap : playerList) {
            state.players.add(Player.builder()
                    .id((String) pMap.get("id"))
                    .nickname((String) pMap.get("nickname"))
                    .build());
        }

        state.currentPlayerIndex = ((Number) map.get("currentPlayerIndex")).intValue();
        state.round = ((Number) map.get("round")).intValue();
        state.rollCount = ((Number) map.get("rollCount")).intValue();

        // 주사위 값 복원
        Object diceObj = map.get("diceValues");
        if (diceObj instanceof List) {
            List<Number> diceList = (List<Number>) diceObj;
            state.diceValues = new int[5];
            for (int i = 0; i < 5; i++) {
                state.diceValues[i] = diceList.get(i).intValue();
            }
        }

        // 홀드 상태 복원
        Object heldObj = map.get("heldDice");
        if (heldObj instanceof List) {
            List<Boolean> heldList = (List<Boolean>) heldObj;
            state.heldDice = new boolean[5];
            for (int i = 0; i < 5; i++) {
                state.heldDice[i] = heldList.get(i);
            }
        }

        // 점수판 복원
        Map<String, Object> scoreboardsMap = (Map<String, Object>) map.get("scoreboards");
        state.scoreboards = new HashMap<>();
        for (Map.Entry<String, Object> entry : scoreboardsMap.entrySet()) {
            Map<String, Object> sbMap = (Map<String, Object>) entry.getValue();
            state.scoreboards.put(entry.getKey(), YachtScoreboard.fromMap(sbMap));
        }

        return state;
    }
}
