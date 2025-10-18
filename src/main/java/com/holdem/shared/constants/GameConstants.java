package com.holdem.shared.constants;

/**
 * Texas Hold'em 遊戲核心常數定義
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
public final class GameConstants {
    
    // 遊戲規則常數
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 10;
    public static final int CARDS_PER_PLAYER = 2;
    public static final int COMMUNITY_CARDS_COUNT = 5;
    public static final int FLOP_CARDS_COUNT = 3;
    public static final int TURN_CARDS_COUNT = 1;
    public static final int RIVER_CARDS_COUNT = 1;
    
    // 撲克牌常數
    public static final int DECK_SIZE = 52;
    public static final int SUITS_COUNT = 4;
    public static final int RANKS_COUNT = 13;
    public static final int BEST_HAND_SIZE = 5;
    public static final int TOTAL_CARDS_FOR_EVALUATION = 7; // 2 hole cards + 5 community cards
    
    // 下注常數
    public static final int SMALL_BLIND_RATIO = 1;
    public static final int BIG_BLIND_RATIO = 2;
    public static final int MAX_RAISES_PER_ROUND = 3;
    
    // 時間限制 (秒)
    public static final int ACTION_TIMEOUT_SECONDS = 30;
    public static final int RECONNECTION_TIMEOUT_SECONDS = 60;
    
    // API 相關常數
    public static final String API_VERSION_V1 = "/api/v1";
    public static final String GAMES_ENDPOINT = "/games";
    public static final String PLAYERS_ENDPOINT = "/players";
    
    // 錯誤代碼
    public static final String INVALID_PLAYER_COUNT = "INVALID_PLAYER_COUNT";
    public static final String INVALID_GAME_STATE = "INVALID_GAME_STATE";
    public static final String INVALID_PLAYER_ACTION = "INVALID_PLAYER_ACTION";
    public static final String INSUFFICIENT_CHIPS = "INSUFFICIENT_CHIPS";
    public static final String DUPLICATE_CARDS_DETECTED = "DUPLICATE_CARDS_DETECTED";
    
    private GameConstants() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}