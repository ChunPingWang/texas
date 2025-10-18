package com.holdem.shared.enums;

/**
 * 遊戲狀態枚舉
 * 定義 Texas Hold'em 遊戲的所有可能狀態
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
public enum GameState {
    WAITING_FOR_PLAYERS("等待玩家加入", false),
    INITIALIZED("遊戲初始化完成", false),
    PRE_FLOP("翻牌前", true),
    FLOP("翻牌", true),
    TURN("轉牌", true),
    RIVER("河牌", true),
    SHOWDOWN("攤牌", true),
    FINISHED("遊戲結束", false);
    
    private final String description;
    private final boolean isActive;
    
    GameState(String description, boolean isActive) {
        this.description = description;
        this.isActive = isActive;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * 檢查是否為下注階段
     */
    public boolean isBettingRound() {
        return this == PRE_FLOP || this == FLOP || this == TURN || this == RIVER;
    }
    
    /**
     * 獲取下一個遊戲狀態
     */
    public GameState getNextState() {
        return switch (this) {
            case WAITING_FOR_PLAYERS -> INITIALIZED;
            case INITIALIZED -> PRE_FLOP;
            case PRE_FLOP -> FLOP;
            case FLOP -> TURN;
            case TURN -> RIVER;
            case RIVER -> SHOWDOWN;
            case SHOWDOWN -> FINISHED;
            case FINISHED -> throw new IllegalStateException("Game is already finished");
        };
    }
}