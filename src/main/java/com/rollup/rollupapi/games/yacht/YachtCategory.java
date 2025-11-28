package com.rollup.rollupapi.games.yacht;

/**
 * 야추 점수 카테고리
 */
public enum YachtCategory {
    // Upper Section
    ONES("ones"),
    TWOS("twos"),
    THREES("threes"),
    FOURS("fours"),
    FIVES("fives"),
    SIXES("sixes"),

    // Lower Section
    CHOICE("choice"),
    FOUR_OF_A_KIND("fourOfAKind"),
    FULL_HOUSE("fullHouse"),
    SMALL_STRAIGHT("smallStraight"),
    LARGE_STRAIGHT("largeStraight"),
    YACHT("yacht");

    private final String key;

    YachtCategory(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static YachtCategory fromKey(String key) {
        for (YachtCategory category : values()) {
            if (category.key.equals(key)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + key);
    }
}
