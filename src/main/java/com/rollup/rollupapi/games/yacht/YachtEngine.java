package com.rollup.rollupapi.games.yacht;

import com.rollup.rollupapi.games.core.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 야추 다이스 게임 엔진
 */
@Component
public class YachtEngine implements GameEngine {

    private static final int MAX_ROLLS = 3;
    private static final int MAX_ROUNDS = 12;

    @Override
    public String getGameType() {
        return "yacht";
    }

    @Override
    public int getMinPlayers() {
        return 1;
    }

    @Override
    public int getMaxPlayers() {
        return 4;
    }

    @Override
    public GameState initGame(List<Player> players, long seed) {
        YachtState state = new YachtState();
        state.setPlayers(new ArrayList<>(players));
        state.setCurrentPlayerIndex(0);
        state.setRound(1);
        state.setDiceValues(new int[]{0, 0, 0, 0, 0});
        state.setHeldDice(new boolean[]{false, false, false, false, false});
        state.setRollCount(0);
        state.setSeed(seed);
        state.setRandom(new Random(seed));

        // 각 플레이어 점수판 초기화
        Map<String, YachtScoreboard> scoreboards = new HashMap<>();
        for (Player player : players) {
            scoreboards.put(player.getId(), new YachtScoreboard());
        }
        state.setScoreboards(scoreboards);

        return state;
    }

    @Override
    public ValidationResult validateAction(GameState state, String playerId, GameAction action) {
        YachtState yachtState = (YachtState) state;
        YachtAction yachtAction = (YachtAction) action;

        // 현재 턴인지 확인
        if (!yachtState.getCurrentPlayerId().equals(playerId)) {
            return ValidationResult.invalid("현재 당신의 턴이 아닙니다");
        }

        switch (yachtAction.getType()) {
            case ROLL_DICE:
                if (yachtState.getRollCount() >= MAX_ROLLS) {
                    return ValidationResult.invalid("더 이상 주사위를 굴릴 수 없습니다 (최대 3회)");
                }
                break;

            case SCORE:
                if (yachtState.getRollCount() == 0) {
                    return ValidationResult.invalid("먼저 주사위를 굴려야 합니다");
                }
                String category = yachtAction.getCategory();
                if (category == null || category.isEmpty()) {
                    return ValidationResult.invalid("점수 카테고리를 선택해야 합니다");
                }
                YachtScoreboard board = yachtState.getScoreboards().get(playerId);
                if (board.isScored(category)) {
                    return ValidationResult.invalid("이미 선택한 카테고리입니다");
                }
                // 유효한 카테고리인지 확인
                try {
                    YachtCategory.fromKey(category);
                } catch (IllegalArgumentException e) {
                    return ValidationResult.invalid("유효하지 않은 카테고리입니다: " + category);
                }
                break;

            default:
                return ValidationResult.invalid("알 수 없는 액션 타입입니다");
        }

        return ValidationResult.valid();
    }

    @Override
    public GameState applyAction(GameState state, String playerId, GameAction action) {
        YachtState yachtState = (YachtState) ((YachtState) state).copy();
        YachtAction yachtAction = (YachtAction) action;

        switch (yachtAction.getType()) {
            case ROLL_DICE:
                rollDice(yachtState, yachtAction.getHeldDice());
                break;

            case SCORE:
                scoreCategory(yachtState, playerId, yachtAction.getCategory());
                nextTurn(yachtState);
                break;
        }

        return yachtState;
    }

    private void rollDice(YachtState state, boolean[] heldDice) {
        int[] dice = state.getDiceValues();
        Random random = state.getRandom();

        if (random == null) {
            random = new Random(state.getSeed());
            state.setRandom(random);
        }

        // 첫 번째 굴림이면 모든 주사위 굴림
        if (state.getRollCount() == 0) {
            for (int i = 0; i < 5; i++) {
                dice[i] = random.nextInt(6) + 1;
            }
        } else {
            // 홀드되지 않은 주사위만 굴림
            for (int i = 0; i < 5; i++) {
                if (heldDice == null || !heldDice[i]) {
                    dice[i] = random.nextInt(6) + 1;
                }
            }
        }

        state.setDiceValues(dice);
        state.setHeldDice(heldDice != null ? heldDice : new boolean[5]);
        state.setRollCount(state.getRollCount() + 1);
    }

    private void scoreCategory(YachtState state, String playerId, String category) {
        int[] dice = state.getDiceValues();
        int score = YachtScoreCalculator.calculate(dice, category);

        YachtScoreboard board = state.getScoreboards().get(playerId);
        board.setScore(category, score);
    }

    private void nextTurn(YachtState state) {
        // 턴 초기화
        state.setRollCount(0);
        state.setHeldDice(new boolean[]{false, false, false, false, false});
        state.setDiceValues(new int[]{0, 0, 0, 0, 0});

        int nextIndex = (state.getCurrentPlayerIndex() + 1) % state.getPlayers().size();
        state.setCurrentPlayerIndex(nextIndex);

        // 한 바퀴 돌았으면 라운드 증가
        if (nextIndex == 0) {
            state.setRound(state.getRound() + 1);
        }
    }

    @Override
    public Optional<GameResultData> checkGameEnd(GameState state) {
        YachtState yachtState = (YachtState) state;

        // 12라운드가 끝났는지 체크 (모든 카테고리 채움)
        boolean allComplete = true;
        for (YachtScoreboard board : yachtState.getScoreboards().values()) {
            if (!board.isComplete()) {
                allComplete = false;
                break;
            }
        }

        if (allComplete || yachtState.getRound() > MAX_ROUNDS) {
            return Optional.of(calculateFinalResult(yachtState));
        }

        return Optional.empty();
    }

    private GameResultData calculateFinalResult(YachtState state) {
        List<GameResultData.PlayerResult> results = new ArrayList<>();

        for (Player player : state.getPlayers()) {
            YachtScoreboard board = state.getScoreboards().get(player.getId());
            results.add(GameResultData.PlayerResult.builder()
                    .playerId(player.getId())
                    .nickname(player.getNickname())
                    .score(board.getTotalScore())
                    .build());
        }

        // 점수로 정렬
        results.sort((a, b) -> b.getScore() - a.getScore());

        // 순위 부여
        for (int i = 0; i < results.size(); i++) {
            results.get(i).setRank(i + 1);
        }

        String winnerId = results.isEmpty() ? null : results.get(0).getPlayerId();

        return GameResultData.builder()
                .winnerId(winnerId)
                .playerResults(results)
                .build();
    }

    @Override
    public Map<String, Object> getPublicState(GameState state) {
        YachtState yachtState = (YachtState) state;
        return yachtState.toMap();
    }

    @Override
    public Map<String, Object> getPrivateState(GameState state, String playerId) {
        // 야추는 비공개 정보 없음 (모든 정보가 공개)
        return Collections.emptyMap();
    }

    @Override
    @SuppressWarnings("unchecked")
    public GameAction parseAction(String actionType, Map<String, Object> payload) {
        YachtActionType type = YachtActionType.valueOf(actionType);

        YachtAction action = new YachtAction();
        action.setType(type);

        if (type == YachtActionType.ROLL_DICE && payload != null) {
            Object heldObj = payload.get("heldDice");
            if (heldObj instanceof List) {
                List<Boolean> heldList = (List<Boolean>) heldObj;
                boolean[] heldDice = new boolean[5];
                for (int i = 0; i < Math.min(5, heldList.size()); i++) {
                    heldDice[i] = heldList.get(i) != null && heldList.get(i);
                }
                action.setHeldDice(heldDice);
            }
        } else if (type == YachtActionType.SCORE && payload != null) {
            action.setCategory((String) payload.get("category"));
        }

        return action;
    }

    @Override
    public GameState deserializeState(Map<String, Object> data) {
        return YachtState.fromMap(data);
    }
}
