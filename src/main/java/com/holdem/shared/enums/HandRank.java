package com.holdem.shared.enums;

/**
 * 牌型強度枚舉
 * 定義 Texas Hold'em 所有牌型及其強度
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
public enum HandRank {
    HIGH_CARD(1, "高牌", "High Card"),
    ONE_PAIR(2, "一對", "One Pair"),
    TWO_PAIR(3, "兩對", "Two Pair"),
    THREE_OF_A_KIND(4, "三條", "Three of a Kind"),
    STRAIGHT(5, "順子", "Straight"),
    FLUSH(6, "同花", "Flush"),
    FULL_HOUSE(7, "葫蘆", "Full House"),
    FOUR_OF_A_KIND(8, "四條", "Four of a Kind"),
    STRAIGHT_FLUSH(9, "同花順", "Straight Flush"),
    ROYAL_FLUSH(10, "皇家同花順", "Royal Flush");
    
    private final int strength;
    private final String chineseName;
    private final String englishName;
    
    HandRank(int strength, String chineseName, String englishName) {
        this.strength = strength;
        this.chineseName = chineseName;
        this.englishName = englishName;
    }
    
    public int getStrength() {
        return strength;
    }
    
    public String getChineseName() {
        return chineseName;
    }
    
    public String getEnglishName() {
        return englishName;
    }
    
    /**
     * 比較兩個牌型強度
     * 
     * @param other 另一個牌型
     * @return 正數表示此牌型更強，負數表示更弱，0表示相等
     */
    public int compareStrength(HandRank other) {
        return Integer.compare(this.strength, other.strength);
    }
    
    /**
     * 檢查是否為同花類型
     */
    public boolean isFlushType() {
        return this == FLUSH || this == STRAIGHT_FLUSH || this == ROYAL_FLUSH;
    }
    
    /**
     * 檢查是否為順子類型
     */
    public boolean isStraightType() {
        return this == STRAIGHT || this == STRAIGHT_FLUSH || this == ROYAL_FLUSH;
    }
}