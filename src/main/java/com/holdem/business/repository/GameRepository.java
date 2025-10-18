package com.holdem.business.repository;

import com.holdem.business.domain.Game;

import java.util.List;
import java.util.Optional;

/**
 * 遊戲資料存取介面
 * 定義遊戲資料的持久化操作，符合依賴反轉原則
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
public interface GameRepository {
    
    /**
     * 保存遊戲
     * @param game 遊戲實例
     * @return 保存後的遊戲實例
     */
    Game save(Game game);
    
    /**
     * 根據ID尋找遊戲
     * @param gameId 遊戲ID
     * @return 遊戲實例（可能為空）
     */
    Optional<Game> findById(String gameId);
    
    /**
     * 獲取所有遊戲
     * @return 遊戲列表
     */
    List<Game> findAll();
    
    /**
     * 根據狀態尋找遊戲
     * @param state 遊戲狀態
     * @return 符合條件的遊戲列表
     */
    List<Game> findByState(String state);
    
    /**
     * 刪除遊戲
     * @param gameId 遊戲ID
     */
    void deleteById(String gameId);
    
    /**
     * 檢查遊戲是否存在
     * @param gameId 遊戲ID
     * @return 是否存在
     */
    boolean existsById(String gameId);
}