package com.holdem.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * 創建遊戲請求 DTO
 * 用於接收和驗證創建遊戲的請求參數
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
@Schema(description = "創建德州撲克遊戲的請求參數")
public class CreateGameRequestDto {
    
    @Schema(
        description = "參與遊戲的玩家姓名列表",
        example = "[\"Alice\", \"Bob\", \"Charlie\"]",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "玩家名單不能為空")
    @Size(min = 2, max = 10, message = "玩家數量必須在2-10人之間")
    private List<String> playerNames;
    
    @Schema(
        description = "小盲注金額（每一手牌較小的強制下注）",
        example = "1.00",
        minimum = "0.01",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "小盲注金額不能為空")
    @DecimalMin(value = "0.01", message = "小盲注金額必須大於0")
    private BigDecimal smallBlind;
    
    @Schema(
        description = "大盲注金額（每一手牌較大的強制下注，通常是小盲注的兩倍）",
        example = "2.00",
        minimum = "0.02",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "大盲注金額不能為空")
    @DecimalMin(value = "0.02", message = "大盲注金額必須大於小盲注")
    private BigDecimal bigBlind;
    
    @Schema(
        description = "每位玩家的初始籌碼數量",
        example = "1000.00",
        minimum = "1.00",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "初始籌碼不能為空")
    @DecimalMin(value = "1.00", message = "初始籌碼必須大於0")
    private BigDecimal initialChips;
    
    // 建構函數
    public CreateGameRequestDto() {
    }
    
    public CreateGameRequestDto(List<String> playerNames, BigDecimal smallBlind, 
                               BigDecimal bigBlind, BigDecimal initialChips) {
        this.playerNames = playerNames;
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
        this.initialChips = initialChips;
    }
    
    // Getter 和 Setter
    public List<String> getPlayerNames() {
        return playerNames;
    }
    
    public void setPlayerNames(List<String> playerNames) {
        this.playerNames = playerNames;
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
    
    public BigDecimal getInitialChips() {
        return initialChips;
    }
    
    public void setInitialChips(BigDecimal initialChips) {
        this.initialChips = initialChips;
    }
}