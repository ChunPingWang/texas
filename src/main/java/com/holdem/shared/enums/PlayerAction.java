package com.holdem.shared.enums;

/**
 * 玩家行動枚舉
 * 定義 Texas Hold'em 中玩家可執行的所有行動
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
public enum PlayerAction {
    FOLD("棄牌", "Fold", false),
    CHECK("過牌", "Check", false),
    CALL("跟注", "Call", true),
    BET("下注", "Bet", true),
    RAISE("加注", "Raise", true),
    ALL_IN("全押", "All-in", true);
    
    private final String chineseName;
    private final String englishName;
    private final boolean requiresChips;
    
    PlayerAction(String chineseName, String englishName, boolean requiresChips) {
        this.chineseName = chineseName;
        this.englishName = englishName;
        this.requiresChips = requiresChips;
    }
    
    public String getChineseName() {
        return chineseName;
    }
    
    public String getEnglishName() {
        return englishName;
    }
    
    public boolean requiresChips() {
        return requiresChips;
    }
    
    /**
     * 檢查行動是否為進攻性行動
     */
    public boolean isAggressive() {
        return this == BET || this == RAISE || this == ALL_IN;
    }
    
    /**
     * 檢查行動是否會結束玩家在該局的參與
     */
    public boolean isTerminating() {
        return this == FOLD;
    }
    
    /**
     * 檢查行動是否為被動行動
     */
    public boolean isPassive() {
        return this == CHECK || this == CALL;
    }
}