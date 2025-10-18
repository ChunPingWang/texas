package com.holdem.presentation.dto;

import com.holdem.shared.enums.PlayerAction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 玩家動作請求 DTO
 * 用於接收和驗證玩家動作的請求參數
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
public class PlayerActionRequestDto {
    
    @NotBlank(message = "玩家ID不能為空")
    private String playerId;
    
    @NotNull(message = "動作類型不能為空")
    private PlayerAction action;
    
    @DecimalMin(value = "0.01", message = "金額必須大於0", groups = RaiseValidation.class)
    private BigDecimal amount;
    
    // 驗證群組介面
    public interface RaiseValidation {}
    
    // 建構函數
    public PlayerActionRequestDto() {
    }
    
    public PlayerActionRequestDto(String playerId, PlayerAction action, BigDecimal amount) {
        this.playerId = playerId;
        this.action = action;
        this.amount = amount;
    }
    
    // Getter 和 Setter
    public String getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    
    public PlayerAction getAction() {
        return action;
    }
    
    public void setAction(PlayerAction action) {
        this.action = action;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}