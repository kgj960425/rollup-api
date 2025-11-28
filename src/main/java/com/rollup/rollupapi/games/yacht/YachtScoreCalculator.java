package com.rollup.rollupapi.games.yacht;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 야추 점수 계산기
 */
public class YachtScoreCalculator {

    /**
     * 주어진 주사위와 카테고리에 대한 점수 계산
     */
    public static int calculate(int[] dice, String category) {
        return switch (category) {
            case "ones" -> sumOfNumber(dice, 1);
            case "twos" -> sumOfNumber(dice, 2);
            case "threes" -> sumOfNumber(dice, 3);
            case "fours" -> sumOfNumber(dice, 4);
            case "fives" -> sumOfNumber(dice, 5);
            case "sixes" -> sumOfNumber(dice, 6);
            case "choice" -> sumAll(dice);
            case "fourOfAKind" -> calculateFourOfAKind(dice);
            case "fullHouse" -> calculateFullHouse(dice);
            case "smallStraight" -> calculateSmallStraight(dice);
            case "largeStraight" -> calculateLargeStraight(dice);
            case "yacht" -> calculateYacht(dice);
            default -> throw new IllegalArgumentException("Unknown category: " + category);
        };
    }

    /**
     * 모든 카테고리에 대한 예상 점수 계산
     */
    public static Map<String, Integer> calculateAll(int[] dice) {
        Map<String, Integer> scores = new HashMap<>();
        for (YachtCategory category : YachtCategory.values()) {
            scores.put(category.getKey(), calculate(dice, category.getKey()));
        }
        return scores;
    }

    /**
     * 특정 숫자의 합 (Upper Section)
     */
    private static int sumOfNumber(int[] dice, int number) {
        int sum = 0;
        for (int d : dice) {
            if (d == number) {
                sum += d;
            }
        }
        return sum;
    }

    /**
     * 주사위 전체 합 (Choice)
     */
    private static int sumAll(int[] dice) {
        int sum = 0;
        for (int d : dice) {
            sum += d;
        }
        return sum;
    }

    /**
     * Four of a Kind - 같은 눈 4개 이상이면 해당 숫자의 합
     */
    private static int calculateFourOfAKind(int[] dice) {
        int[] counts = getCounts(dice);
        for (int i = 1; i <= 6; i++) {
            if (counts[i] >= 4) {
                return i * 4;
            }
        }
        return 0;
    }

    /**
     * Full House - 3개 + 2개 같은 눈이면 전체 합
     */
    private static int calculateFullHouse(int[] dice) {
        int[] counts = getCounts(dice);
        boolean hasThree = false;
        boolean hasTwo = false;

        for (int i = 1; i <= 6; i++) {
            if (counts[i] == 3) hasThree = true;
            if (counts[i] == 2) hasTwo = true;
        }

        if (hasThree && hasTwo) {
            return sumAll(dice);
        }
        return 0;
    }

    /**
     * Small Straight - 연속 4개 (1-2-3-4, 2-3-4-5, 3-4-5-6)
     */
    private static int calculateSmallStraight(int[] dice) {
        int[] counts = getCounts(dice);

        // 1-2-3-4
        if (counts[1] >= 1 && counts[2] >= 1 && counts[3] >= 1 && counts[4] >= 1) {
            return 15;
        }
        // 2-3-4-5
        if (counts[2] >= 1 && counts[3] >= 1 && counts[4] >= 1 && counts[5] >= 1) {
            return 15;
        }
        // 3-4-5-6
        if (counts[3] >= 1 && counts[4] >= 1 && counts[5] >= 1 && counts[6] >= 1) {
            return 15;
        }

        return 0;
    }

    /**
     * Large Straight - 연속 5개 (1-2-3-4-5 또는 2-3-4-5-6)
     */
    private static int calculateLargeStraight(int[] dice) {
        int[] sorted = Arrays.copyOf(dice, dice.length);
        Arrays.sort(sorted);

        // 1-2-3-4-5
        if (sorted[0] == 1 && sorted[1] == 2 && sorted[2] == 3 && sorted[3] == 4 && sorted[4] == 5) {
            return 30;
        }
        // 2-3-4-5-6
        if (sorted[0] == 2 && sorted[1] == 3 && sorted[2] == 4 && sorted[3] == 5 && sorted[4] == 6) {
            return 30;
        }

        return 0;
    }

    /**
     * Yacht - 5개 모두 같은 눈이면 50점
     */
    private static int calculateYacht(int[] dice) {
        int[] counts = getCounts(dice);
        for (int i = 1; i <= 6; i++) {
            if (counts[i] == 5) {
                return 50;
            }
        }
        return 0;
    }

    /**
     * 주사위 눈별 개수 카운트 (인덱스 1~6 사용)
     */
    private static int[] getCounts(int[] dice) {
        int[] counts = new int[7];  // 인덱스 0은 사용 안 함
        for (int d : dice) {
            counts[d]++;
        }
        return counts;
    }
}
