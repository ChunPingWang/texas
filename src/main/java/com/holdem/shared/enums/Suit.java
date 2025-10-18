package com.holdem.shared.enums;

/**
 * 撲克牌花色枚舉
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
public enum Suit {
    SPADES("♠", "黑桃", 4),
    HEARTS("♥", "紅心", 3),
    DIAMONDS("♦", "方塊", 2),
    CLUBS("♣", "梅花", 1);
    
    private final String symbol;
    private final String chineseName;
    private final int priority; // 用於比較，黑桃最大
    
    Suit(String symbol, String chineseName, int priority) {
        this.symbol = symbol;
        this.chineseName = chineseName;
        this.priority = priority;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public String getChineseName() {
        return chineseName;
    }
    
    public int getPriority() {
        return priority;
    }
}