package com.holdem.persistence.entity;

import com.holdem.shared.enums.GameState;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 遊戲實體類
 * 用於資料庫持久化的遊戲實體，符合 JPA 規範
 * 只依賴於 shared 層的枚舉類型
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
@Entity
@Table(name = "games")
public class GameEntity {
    
    @Id
    private String id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "game_state", nullable = false)
    private GameState state;
    
    @Column(name = "small_blind", precision = 10, scale = 2, nullable = false)
    private BigDecimal smallBlind;
    
    @Column(name = "big_blind", precision = 10, scale = 2, nullable = false)
    private BigDecimal bigBlind;
    
    @Column(name = "current_pot", precision = 10, scale = 2)
    private BigDecimal currentPot;
    
    @Column(name = "current_bet", precision = 10, scale = 2)
    private BigDecimal currentBet;
    
    @Column(name = "dealer_position")
    private Integer dealerPosition;
    
    @Column(name = "current_player_index")
    private Integer currentPlayerIndex;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PlayerEntity> players = new ArrayList<>();
    
    // 建構函數
    public GameEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public GameEntity(String id, GameState state, BigDecimal smallBlind, BigDecimal bigBlind) {
        this();
        this.id = id;
        this.state = state;
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
        this.currentPot = BigDecimal.ZERO;
        this.currentBet = BigDecimal.ZERO;
    }
    
    // JPA 生命週期回調
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getter 和 Setter
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public GameState getState() {
        return state;
    }
    
    public void setState(GameState state) {
        this.state = state;
    }
    
    public BigDecimal getSmallBlind() {
        return smallBlind;
    }
    
    public void setSmallBlind(BigDecimal smallBlind) {
        this.smallBlind = smallBlind;
    }
    
    public BigDecimal getBigBlind() {
        return bigBlind;
    }
    
    public void setBigBlind(BigDecimal bigBlind) {
        this.bigBlind = bigBlind;
    }
    
    public BigDecimal getCurrentPot() {
        return currentPot;
    }
    
    public void setCurrentPot(BigDecimal currentPot) {
        this.currentPot = currentPot;
    }
    
    public BigDecimal getCurrentBet() {
        return currentBet;
    }
    
    public void setCurrentBet(BigDecimal currentBet) {
        this.currentBet = currentBet;
    }
    
    public Integer getDealerPosition() {
        return dealerPosition;
    }
    
    public void setDealerPosition(Integer dealerPosition) {
        this.dealerPosition = dealerPosition;
    }
    
    public Integer getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }
    
    public void setCurrentPlayerIndex(Integer currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<PlayerEntity> getPlayers() {
        return players;
    }
    
    public void setPlayers(List<PlayerEntity> players) {
        this.players = players;
    }
    
    // 便利方法
    public void addPlayer(PlayerEntity player) {
        players.add(player);
        player.setGame(this);
    }
    
    public void removePlayer(PlayerEntity player) {
        players.remove(player);
        player.setGame(null);
    }
}