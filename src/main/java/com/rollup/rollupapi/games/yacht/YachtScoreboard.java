package com.rollup.rollupapi.games.yacht;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 야추 점수판
 */
@Data
@NoArgsConstructor
public class YachtScoreboard {
    private Map<String, Integer> scores = new HashMap<>();

    public boolean isScored(String category) {
        return scores.containsKey(category);
    }

    public void setScore(String category, int score) {
        scores.put(category, score);
    }

    public Integer getScore(String category) {
        return scores.get(category);
    }

    public int getTotalScore() {
        int upperSum = 0;
        int lowerSum = 0;

        // Upper Section
        String[] upperCategories = {"ones", "twos", "threes", "fours", "fives", "sixes"};
        for (String cat : upperCategories) {
            Integer score = scores.get(cat);
            if (score != null) {
                upperSum += score;
            }
        }

        // Upper Bonus (63점 이상이면 +35점)
        int bonus = upperSum >= 63 ? 35 : 0;

        // Lower Section
        String[] lowerCategories = {"choice", "fourOfAKind", "fullHouse", "smallStraight", "largeStraight", "yacht"};
        for (String cat : lowerCategories) {
            Integer score = scores.get(cat);
            if (score != null) {
                lowerSum += score;
            }
        }

        return upperSum + bonus + lowerSum;
    }

    public int getUpperSectionSum() {
        String[] upperCategories = {"ones", "twos", "threes", "fours", "fives", "sixes"};
        int sum = 0;
        for (String cat : upperCategories) {
            Integer score = scores.get(cat);
            if (score != null) {
                sum += score;
            }
        }
        return sum;
    }

    public boolean isComplete() {
        return scores.size() == 12;
    }

    public YachtScoreboard copy() {
        YachtScoreboard copy = new YachtScoreboard();
        copy.scores = new HashMap<>(this.scores);
        return copy;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        for (YachtCategory category : YachtCategory.values()) {
            String key = category.getKey();
            map.put(key, scores.get(key));  // null이면 null로 저장
        }
        map.put("total", getTotalScore());
        map.put("upperSum", getUpperSectionSum());
        map.put("bonus", getUpperSectionSum() >= 63 ? 35 : 0);
        return map;
    }

    public static YachtScoreboard fromMap(Map<String, Object> map) {
        YachtScoreboard scoreboard = new YachtScoreboard();
        for (YachtCategory category : YachtCategory.values()) {
            String key = category.getKey();
            Object value = map.get(key);
            if (value != null) {
                scoreboard.setScore(key, ((Number) value).intValue());
            }
        }
        return scoreboard;
    }
}
