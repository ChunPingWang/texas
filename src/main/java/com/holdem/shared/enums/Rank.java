package com.holdem.shared.enums;

/**
 * 撲克牌點數枚舉
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
public enum Rank {
    TWO(2, "2"),
    THREE(3, "3"),
    FOUR(4, "4"),
    FIVE(5, "5"),
    SIX(6, "6"),
    SEVEN(7, "7"),
    EIGHT(8, "8"),
    NINE(9, "9"),
    TEN(10, "T"),
    JACK(11, "J"),
    QUEEN(12, "Q"),
    KING(13, "K"),
    ACE(14, "A"); // 在 Texas Hold'em 中，A 通常被視為最大
    
    private final int value;
    private final String symbol;
    
    Rank(int value, String symbol) {
        this.value = value;
        this.symbol = symbol;
    }
    
    public int getValue() {
        return value;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    /**
     * 檢查是否為面牌 (J, Q, K)
     */
    public boolean isFaceCard() {
        return this == JACK || this == QUEEN || this == KING;
    }
    
    /**
     * 檢查是否為 Ace
     */
    public boolean isAce() {
        return this == ACE;
    }
    
    /**
     * 獲取下一個點數 (用於順子判定)
     */
    public Rank getNext() {
        return switch (this) {
            case TWO -> THREE;
            case THREE -> FOUR;
            case FOUR -> FIVE;
            case FIVE -> SIX;
            case SIX -> SEVEN;
            case SEVEN -> EIGHT;
            case EIGHT -> NINE;
            case NINE -> TEN;
            case TEN -> JACK;
            case JACK -> QUEEN;
            case QUEEN -> KING;
            case KING -> ACE;
            case ACE -> null; // ACE 是最大，沒有下一個
        };
    }
}