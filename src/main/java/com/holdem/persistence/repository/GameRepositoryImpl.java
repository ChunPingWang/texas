package com.holdem.persistence.repository;

import com.holdem.business.domain.Game;
import com.holdem.business.repository.GameRepository;
import com.holdem.persistence.entity.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 遊戲資料存取實現
 * 實現 business 層定義的 GameRepository 介面
 * 符合依賴反轉原則，persistence 層依賴於 business 層的介面
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
@Repository
public interface GameRepositoryImpl extends JpaRepository<GameEntity, String>, GameRepository {
    
    /**
     * 根據狀態查找遊戲實體
     * @param state 遊戲狀態
     * @return 遊戲實體列表
     */
    @Query("SELECT g FROM GameEntity g WHERE g.state = :state")
    List<GameEntity> findEntitiesByState(@Param("state") String state);
    
    /**
     * 查找活躍的遊戲
     * @return 活躍遊戲列表
     */
    @Query("SELECT g FROM GameEntity g WHERE g.state IN ('WAITING_FOR_PLAYERS', 'PLAYING')")
    List<GameEntity> findActiveGames();
    
    // 實現 GameRepository 介面的方法需要在服務層轉換 Domain 和 Entity
}