package com.holdem.persistence.entity;

import com.holdem.shared.enums.PlayerStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * 玩家實體類
 * 用於資料庫持久化的玩家實體，符合 JPA 規範
 * 只依賴於 shared 層的枚舉類型
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
@Entity
@Table(name = "players")
public class PlayerEntity {
    
    @Id
    private String id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "chip_count", precision = 10, scale = 2, nullable = false)
    private BigDecimal chipCount;
    
    @Column(name = "current_bet", precision = 10, scale = 2)
    private BigDecimal currentBet;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PlayerStatus status;
    
    @Column(name = "position")
    private Integer position;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private GameEntity game;
    
    // 建構函數
    public PlayerEntity() {
        this.currentBet = BigDecimal.ZERO;
        this.status = PlayerStatus.ACTIVE;
    }
    
    public PlayerEntity(String id, String name, BigDecimal chipCount) {
        this();
        this.id = id;
        this.name = name;
        this.chipCount = chipCount;
    }
    
    // Getter 和 Setter
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public BigDecimal getChipCount() {
        return chipCount;
    }
    
    public void setChipCount(BigDecimal chipCount) {
        this.chipCount = chipCount;
    }
    
    public BigDecimal getCurrentBet() {
        return currentBet;
    }
    
    public void setCurrentBet(BigDecimal currentBet) {
        this.currentBet = currentBet;
    }
    
    public PlayerStatus getStatus() {
        return status;
    }
    
    public void setStatus(PlayerStatus status) {
        this.status = status;
    }
    
    public Integer getPosition() {
        return position;
    }
    
    public void setPosition(Integer position) {
        this.position = position;
    }
    
    public GameEntity getGame() {
        return game;
    }
    
    public void setGame(GameEntity game) {
        this.game = game;
    }
}