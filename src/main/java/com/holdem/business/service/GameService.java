package com.holdem.business.service;

import com.holdem.business.domain.Game;
import com.holdem.business.domain.Player;
import com.holdem.shared.enums.PlayerAction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 遊戲服務接口
 * 定義德州撲克遊戲的核心業務操作
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
public interface GameService {
    
    /**
     * 創建新遊戲
     * @param playerNames 玩家姓名列表
     * @param smallBlind 小盲注金額
     * @param bigBlind 大盲注金額
     * @param initialChips 初始籌碼
     * @return 創建的遊戲實例
     */
    Game createGame(List<String> playerNames, BigDecimal smallBlind, BigDecimal bigBlind, BigDecimal initialChips);
    
    /**
     * 開始新一手牌
     * @param gameId 遊戲ID
     * @return 更新後的遊戲狀態
     */
    Game startNewHand(String gameId);
    
    /**
     * 玩家執行動作
     * @param gameId 遊戲ID
     * @param playerId 玩家ID
     * @param action 動作類型
     * @param amount 金額（用於加注）
     * @return 更新後的遊戲狀態
     */
    Game playerAction(String gameId, String playerId, PlayerAction action, BigDecimal amount);
    
    /**
     * 獲取遊戲狀態
     * @param gameId 遊戲ID
     * @return 遊戲實例
     */
    Game getGame(String gameId);
    
    /**
     * 檢查遊戲是否結束
     * @param gameId 遊戲ID
     * @return 是否結束
     */
    boolean isGameFinished(String gameId);
    
    /**
     * 獲取遊戲中的所有玩家
     * @param gameId 遊戲ID
     * @return 玩家列表
     */
    List<Player> getPlayers(String gameId);
}