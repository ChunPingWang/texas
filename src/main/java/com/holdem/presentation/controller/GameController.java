package com.holdem.presentation.controller;

import com.holdem.business.domain.Game;
import com.holdem.business.domain.Player;
import com.holdem.business.service.GameService;
import com.holdem.presentation.dto.CreateGameRequestDto;
import com.holdem.presentation.dto.PlayerActionRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 遊戲控制器
 * 處理德州撲克遊戲相關的 HTTP 請求
 * 只依賴於 business 層的服務介面，符合依賴反轉原則
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
@RestController
@RequestMapping("/api/games")
@CrossOrigin(origins = "*")
@Tag(name = "Game Management", description = "德州撲克遊戲管理 API")
public class GameController {
    
    private final GameService gameService;
    
    /**
     * 建構函數注入，依賴於業務層介面
     * @param gameService 遊戲服務介面
     */
    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }
    
    /**
     * 創建新遊戲
     * @param request 創建遊戲請求
     * @return 創建的遊戲資訊
     */
    @Operation(
        summary = "創建新的德州撲克遊戲",
        description = "創建一個新的德州撲克遊戲，支援 2-10 名玩家。系統會自動初始化遊戲狀態、分配初始籌碼並設置盲注。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "遊戲創建成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Game.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "請求參數無效 - 玩家數量不在 2-10 範圍內，或盲注設置不合理"
        )
    })
    @PostMapping
    public ResponseEntity<Game> createGame(
        @Parameter(description = "創建遊戲的請求參數", required = true)
        @Valid @RequestBody CreateGameRequestDto request) {
        try {
            Game game = gameService.createGame(
                request.getPlayerNames(),
                request.getSmallBlind(),
                request.getBigBlind(),
                request.getInitialChips()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(game);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 獲取遊戲狀態
     * @param gameId 遊戲ID
     * @return 遊戲資訊
     */
    @Operation(
        summary = "獲取遊戲詳細狀態",
        description = "根據遊戲 ID 獲取完整的遊戲狀態資訊，包括所有玩家資訊、當前牌局狀態、底池金額等。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功獲取遊戲狀態",
            content = @Content(schema = @Schema(implementation = Game.class))
        ),
        @ApiResponse(responseCode = "404", description = "遊戲不存在")
    })
    @GetMapping("/{gameId}")
    public ResponseEntity<Game> getGame(
        @Parameter(description = "遊戲唯一識別碼", required = true, example = "game-123")
        @PathVariable String gameId) {
        Game game = gameService.getGame(gameId);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(game);
    }
    
    /**
     * 開始新一手牌
     * @param gameId 遊戲ID
     * @return 更新後的遊戲狀態
     */
    @PostMapping("/{gameId}/start-hand")
    public ResponseEntity<Game> startNewHand(@PathVariable String gameId) {
        try {
            Game game = gameService.startNewHand(gameId);
            return ResponseEntity.ok(game);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 執行玩家動作
     * @param gameId 遊戲ID
     * @param request 玩家動作請求
     * @return 更新後的遊戲狀態
     */
    @PostMapping("/{gameId}/actions")
    public ResponseEntity<Game> playerAction(@PathVariable String gameId, 
                                           @Valid @RequestBody PlayerActionRequestDto request) {
        try {
            Game game = gameService.playerAction(
                gameId,
                request.getPlayerId(),
                request.getAction(),
                request.getAmount()
            );
            return ResponseEntity.ok(game);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 獲取遊戲中的所有玩家
     * @param gameId 遊戲ID
     * @return 玩家列表
     */
    @GetMapping("/{gameId}/players")
    public ResponseEntity<List<Player>> getPlayers(@PathVariable String gameId) {
        List<Player> players = gameService.getPlayers(gameId);
        if (players == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(players);
    }
    
    /**
     * 檢查遊戲是否結束
     * @param gameId 遊戲ID
     * @return 遊戲結束狀態
     */
    @GetMapping("/{gameId}/finished")
    public ResponseEntity<Boolean> isGameFinished(@PathVariable String gameId) {
        boolean finished = gameService.isGameFinished(gameId);
        return ResponseEntity.ok(finished);
    }
}