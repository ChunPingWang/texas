package com.holdem.bdd.steps;

import com.holdem.business.domain.Card;
import com.holdem.business.domain.Hand;
import com.holdem.business.domain.Player;
import com.holdem.business.service.HandEvaluationService;
import com.holdem.shared.enums.HandRank;
import com.holdem.shared.enums.Suit;
import com.holdem.shared.enums.Rank;
import io.cucumber.java.zh_tw.假設;
import io.cucumber.java.zh_tw.當;
import io.cucumber.java.zh_tw.那麼;
import io.cucumber.java.zh_tw.而且;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 牌型判定相關的 BDD 步驟定義
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
public class HandEvaluationSteps {
    
    private HandEvaluationService handEvaluationService;
    private List<Card> playerHoleCards;
    private List<Card> playerBHoleCards;
    private List<Card> communityCards;
    private Hand evaluatedHand;
    private Player playerA;
    private Player playerB;
    private int comparisonResult;
    private String winReason;
    
    public HandEvaluationSteps() {
        this.handEvaluationService = new HandEvaluationService();
        this.playerHoleCards = new ArrayList<>();
        this.playerBHoleCards = new ArrayList<>();
        this.communityCards = new ArrayList<>();
    }
    
    @假設("玩家持有底牌 {string}")
    public void 玩家持有底牌(String cards) {
        playerHoleCards = parseCards(cards);
    }
    
    @而且("公共牌為 {string}")  
    public void 公共牌為(String cards) {
        communityCards = parseCards(cards);
    }
    
    @當("系統計算玩家的最佳牌型時")
    public void 系統計算玩家的最佳牌型時() {
        List<Card> allCards = new ArrayList<>();
        allCards.addAll(playerHoleCards);
        allCards.addAll(communityCards);
        evaluatedHand = handEvaluationService.evaluateBestHand(allCards);
    }
    
    @那麼("牌型應該是 {string}")
    public void 牌型應該是(String expectedHandRank) {
        HandRank expected = parseHandRank(expectedHandRank);
        assertEquals(expected, evaluatedHand.getRank(), 
            "Expected hand rank: " + expected + ", but was: " + evaluatedHand.getRank());
    }
    
    @而且("牌型強度應該是最高等級")
    public void 牌型強度應該是最高等級() {
        assertEquals(HandRank.ROYAL_FLUSH, evaluatedHand.getRank(),
            "Royal flush should be the highest rank");
    }
    
    @而且("關鍵牌應該是 {string}")
    public void 關鍵牌應該是(String expectedKey) {
        Rank expectedRank = parseRank(expectedKey);
        assertEquals(expectedRank, evaluatedHand.getKeyCard(),
            "Expected key card: " + expectedRank + ", but was: " + evaluatedHand.getKeyCard());
    }
    
    @而且("四條牌值應該是 {string}")
    public void 四條牌值應該是(String expectedRank) {
        Rank expected = parseRank(expectedRank);
        assertEquals(expected, evaluatedHand.getKeyCard(),
            "Expected four of a kind rank: " + expected);
    }
    
    @而且("踢腳牌應該是 {string}")
    public void 踢腳牌應該是(String expectedKicker) {
        Rank expected = parseRank(expectedKicker);
        List<Rank> kickers = evaluatedHand.getKickers();
        assertFalse(kickers.isEmpty(), "Should have at least one kicker");
        assertEquals(expected, kickers.get(0),
            "Expected kicker: " + expected + ", but was: " + kickers.get(0));
    }
    
    @而且("三條牌值應該是 {string}")
    public void 三條牌值應該是(String expectedRank) {
        Rank expected = parseRank(expectedRank);
        assertEquals(expected, evaluatedHand.getKeyCard(),
            "Expected three of a kind rank: " + expected);
    }
    
    @而且("對子牌值應該是 {string}")
    public void 對子牌值應該是(String expectedRank) {
        Rank expected = parseRank(expectedRank);
        List<Rank> pairs = evaluatedHand.getPairs();
        assertFalse(pairs.isEmpty(), "Should have at least one pair");
        assertTrue(pairs.contains(expected),
            "Expected pair rank: " + expected + " in pairs: " + pairs);
    }
    
    @而且("關鍵牌應該依序是 {string}")
    public void 關鍵牌應該依序是(String expectedCards) {
        List<Rank> expected = parseRanks(expectedCards);
        List<Rank> actual = evaluatedHand.getKeyCards();
        assertEquals(expected, actual,
            "Expected key cards: " + expected + ", but was: " + actual);
    }
    
    @而且("高對牌值應該是 {string}")
    public void 高對牌值應該是(String expectedRank) {
        Rank expected = parseRank(expectedRank);
        List<Rank> pairs = evaluatedHand.getPairs();
        assertTrue(pairs.size() >= 2, "Should have at least two pairs");
        assertEquals(expected, pairs.get(0),
            "Expected higher pair: " + expected + ", but was: " + pairs.get(0));
    }
    
    @而且("低對牌值應該是 {string}")
    public void 低對牌值應該是(String expectedRank) {
        Rank expected = parseRank(expectedRank);
        List<Rank> pairs = evaluatedHand.getPairs();
        assertTrue(pairs.size() >= 2, "Should have at least two pairs");
        assertEquals(expected, pairs.get(1),
            "Expected lower pair: " + expected + ", but was: " + pairs.get(1));
    }
    
    @而且("踢腳牌應該依序是 {string}")
    public void 踢腳牌應該依序是(String expectedKickers) {
        List<Rank> expected = parseRanks(expectedKickers);
        List<Rank> actual = evaluatedHand.getKickers();
        assertEquals(expected, actual,
            "Expected kickers: " + expected + ", but was: " + actual);
    }
    
    // 比較相關步驟
    @假設("玩家A持有 {string} 底牌和公共牌 {string}")
    public void 玩家A持有底牌和公共牌(String holeCards, String communityCardsStr) {
        playerHoleCards = parseCards(holeCards);
        communityCards = parseCards(communityCardsStr);
        
        playerA = new Player("PlayerA");
        // 牌型評估將在比較步驟中進行
    }
    
    @而且("玩家B持有 {string} 底牌和相同公共牌")
    public void 玩家B持有底牌和相同公共牌(String holeCards) {
        List<Card> playerBHole = parseCards(holeCards);
        playerBHoleCards = playerBHole; // 儲存玩家B的底牌，稍後與公共牌組合
        
        playerB = new Player("PlayerB");
        // 牌型評估將在比較步驟中進行
    }
    
    @假設("玩家A持有底牌 {string}")
    public void 玩家A持有底牌(String holeCards) {
        playerHoleCards = parseCards(holeCards);
        playerA = new Player("PlayerA");
    }
    
    @假設("玩家B持有底牌 {string}")
    public void 玩家B持有底牌(String holeCards) {
        playerBHoleCards = parseCards(holeCards);
        playerB = new Player("PlayerB");
    }
    
    @假設("玩家A持有三條Q的牌型")
    public void 玩家A持有三條Q的牌型() {
        playerA = new Player("PlayerA");
        // 創建三條Q的測試牌型
        Hand handA = new Hand(HandRank.THREE_OF_A_KIND);
        handA.setKeyCard(Rank.QUEEN);
        handA.setKickers(Arrays.asList(Rank.KING, Rank.JACK));
        playerA.setHand(handA);
    }
    
    @而且("玩家B持有兩對A和K的牌型")
    public void 玩家B持有兩對A和K的牌型() {
        playerB = new Player("PlayerB");
        // 創建兩對A和K的測試牌型
        Hand handB = new Hand(HandRank.TWO_PAIR);
        handB.setPairs(Arrays.asList(Rank.ACE, Rank.KING));
        handB.setKickers(Arrays.asList(Rank.QUEEN));
        playerB.setHand(handB);
    }
    
    @當("系統比較兩位玩家的牌型強度時")
    public void 系統比較兩位玩家的牌型強度時() {
        // 如果還沒有評估玩家的牌型，現在評估
        if (playerA.getHand() == null && playerHoleCards != null && communityCards != null) {
            List<Card> allCardsA = new ArrayList<>();
            allCardsA.addAll(playerHoleCards);
            allCardsA.addAll(communityCards);
            playerA.setHand(handEvaluationService.evaluateBestHand(allCardsA));
        }
        
        if (playerB.getHand() == null && playerBHoleCards != null && communityCards != null) {
            List<Card> allCardsB = new ArrayList<>();
            allCardsB.addAll(playerBHoleCards);
            allCardsB.addAll(communityCards);
            playerB.setHand(handEvaluationService.evaluateBestHand(allCardsB));
        }
        
        comparisonResult = handEvaluationService.compareHands(
            playerA.getHand(), playerB.getHand());
        winReason = handEvaluationService.getWinReason(
            playerA.getHand(), playerB.getHand());
    }
    
    @那麼("玩家A應該勝過玩家B")
    public void 玩家A應該勝過玩家B() {
        assertTrue(comparisonResult > 0,
            "Player A should win over Player B, but comparison result was: " + comparisonResult);
    }
    
    @而且("勝利原因應該是 {string}")
    public void 勝利原因應該是(String expectedReason) {
        assertNotNull(winReason, "Win reason should not be null");
        assertTrue(winReason.contains(expectedReason) || 
                  winReason.toLowerCase().contains(expectedReason.toLowerCase()),
            "Expected win reason to contain: " + expectedReason + ", but was: " + winReason);
    }
    
    @那麼("結果應該是平手")
    public void 結果應該是平手() {
        assertEquals(0, comparisonResult,
            "Result should be a tie, but was: " + comparisonResult);
    }
    
    @而且("兩位玩家應該平分獎池")
    public void 兩位玩家應該平分獎池() {
        // 這個驗證留給分獎池的邏輯
        assertEquals(0, comparisonResult, "Players should split the pot on a tie");
    }
    
    @而且("A應該被視為低牌")
    public void A應該被視為低牌() {
        // 在 A-2-3-4-5 順子中，A 被視為 1
        assertTrue(evaluatedHand.isLowAceStraight(),
            "Ace should be treated as low card in A-2-3-4-5 straight");
    }
    
    // 輔助方法
    private List<Card> parseCards(String cardsString) {
        List<Card> cards = new ArrayList<>();
        String[] cardStrings = cardsString.split("\\s+");
        
        for (String cardString : cardStrings) {
            cards.add(parseCard(cardString.trim()));
        }
        
        return cards;
    }
    
    private Card parseCard(String cardString) {
        if (cardString.length() < 2) {
            throw new IllegalArgumentException("Invalid card format: " + cardString);
        }
        
        String rankStr = cardString.substring(0, cardString.length() - 1);
        String suitStr = cardString.substring(cardString.length() - 1);
        
        Rank rank = parseRank(rankStr);
        Suit suit = parseSuit(suitStr);
        
        return new Card(rank, suit);
    }
    
    private Rank parseRank(String rankStr) {
        switch (rankStr.toUpperCase()) {
            case "A": return Rank.ACE;
            case "K": return Rank.KING;
            case "Q": return Rank.QUEEN;
            case "J": return Rank.JACK;
            case "10": return Rank.TEN;
            case "9": return Rank.NINE;
            case "8": return Rank.EIGHT;
            case "7": return Rank.SEVEN;
            case "6": return Rank.SIX;
            case "5": return Rank.FIVE;
            case "4": return Rank.FOUR;
            case "3": return Rank.THREE;
            case "2": return Rank.TWO;
            default: throw new IllegalArgumentException("Invalid rank: " + rankStr);
        }
    }
    
    private Suit parseSuit(String suitStr) {
        switch (suitStr) {
            case "♠": return Suit.SPADES;
            case "♥": return Suit.HEARTS;
            case "♦": return Suit.DIAMONDS;
            case "♣": return Suit.CLUBS;
            default: throw new IllegalArgumentException("Invalid suit: " + suitStr);
        }
    }
    
    private HandRank parseHandRank(String handRankStr) {
        switch (handRankStr) {
            case "皇家同花順": return HandRank.ROYAL_FLUSH;
            case "同花順": return HandRank.STRAIGHT_FLUSH;
            case "四條": return HandRank.FOUR_OF_A_KIND;
            case "葫蘆": return HandRank.FULL_HOUSE;
            case "同花": return HandRank.FLUSH;
            case "順子": return HandRank.STRAIGHT;
            case "三條": return HandRank.THREE_OF_A_KIND;
            case "兩對": return HandRank.TWO_PAIR;
            case "一對": return HandRank.ONE_PAIR;
            case "高牌": return HandRank.HIGH_CARD;
            default: throw new IllegalArgumentException("Invalid hand rank: " + handRankStr);
        }
    }
    
    private List<Rank> parseRanks(String ranksString) {
        List<Rank> ranks = new ArrayList<>();
        String[] rankStrings = ranksString.split("\\s+");
        
        for (String rankString : rankStrings) {
            ranks.add(parseRank(rankString.trim()));
        }
        
        return ranks;
    }
}