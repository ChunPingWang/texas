package com.holdem.business.service;

import com.holdem.business.domain.Game;
import com.holdem.business.domain.Player;
import com.holdem.shared.enums.PlayerAction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 遊戲服務實現類
 * 實現德州撲克遊戲的核心業務邏輯
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
@Service
public class GameServiceImpl implements GameService {
    
    private final Map<String, Game> gameStore = new ConcurrentHashMap<>();
    
    @Override
    public Game createGame(List<String> playerNames, BigDecimal smallBlind, BigDecimal bigBlind, BigDecimal initialChips) {
        if (playerNames == null || playerNames.size() < 2 || playerNames.size() > 10) {
            throw new IllegalArgumentException("玩家數量必須在2-10人之間");
        }
        
        Game game = new Game();
        game.initialize(playerNames, smallBlind, bigBlind, initialChips);
        gameStore.put(game.getId(), game);
        
        return game;
    }
    
    @Override
    public Game startNewHand(String gameId) {
        Game game = getGame(gameId);
        if (game == null) {
            throw new IllegalArgumentException("遊戲不存在");
        }
        
        game.startNewHand();
        return game;
    }
    
    @Override
    public Game playerAction(String gameId, String playerId, PlayerAction action, BigDecimal amount) {
        Game game = getGame(gameId);
        if (game == null) {
            throw new IllegalArgumentException("遊戲不存在");
        }
        
        Player player = game.getPlayer(playerId);
        if (player == null) {
            throw new IllegalArgumentException("玩家不存在");
        }
        
        switch (action) {
            case FOLD:
                game.playerFold(player);
                break;
            case CALL:
                game.playerCall(player);
                break;
            case RAISE:
                if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("加注金額必須大於0");
                }
                game.playerRaise(player, amount);
                break;
            case ALL_IN:
                game.playerAllIn(player);
                break;
            default:
                throw new IllegalArgumentException("不支援的動作類型");
        }
        
        return game;
    }
    
    @Override
    public Game getGame(String gameId) {
        return gameStore.get(gameId);
    }
    
    @Override
    public boolean isGameFinished(String gameId) {
        Game game = getGame(gameId);
        return game != null && game.isFinished();
    }
    
    @Override
    public List<Player> getPlayers(String gameId) {
        Game game = getGame(gameId);
        return game != null ? game.getPlayers() : null;
    }
}