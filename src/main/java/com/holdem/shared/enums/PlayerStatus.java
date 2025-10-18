package com.holdem.shared.enums;

/**
 * 玩家狀態枚舉
 * 定義玩家在遊戲中的所有可能狀態
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
public enum PlayerStatus {
    WAITING("等待中", "Waiting"),
    ACTIVE("活躍", "Active"),
    FOLDED("已棄牌", "Folded"),
    ALL_IN("全押", "All-in"),
    DISCONNECTED("斷線", "Disconnected"),
    ELIMINATED("淘汰", "Eliminated");
    
    private final String chineseName;
    private final String englishName;
    
    PlayerStatus(String chineseName, String englishName) {
        this.chineseName = chineseName;
        this.englishName = englishName;
    }
    
    public String getChineseName() {
        return chineseName;
    }
    
    public String getEnglishName() {
        return englishName;
    }
    
    /**
     * 檢查玩家是否仍在遊戲中
     */
    public boolean isInGame() {
        return this == ACTIVE || this == ALL_IN;
    }
    
    /**
     * 檢查玩家是否可以執行行動
     */
    public boolean canAct() {
        return this == ACTIVE;
    }
    
    /**
     * 檢查玩家是否已退出當前手牌
     */
    public boolean isOutOfHand() {
        return this == FOLDED || this == ELIMINATED;
    }
}