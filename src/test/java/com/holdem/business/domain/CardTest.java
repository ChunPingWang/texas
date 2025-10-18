package com.holdem.business.domain;

import com.holdem.shared.enums.Rank;
import com.holdem.shared.enums.Suit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Card 領域實體的單元測試
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
@DisplayName("Card 領域實體測試")
class CardTest {
    
    @Test
    @DisplayName("應能成功建立撲克牌實例")
    void shouldCreateCardSuccessfully() {
        // Given
        Rank rank = Rank.ACE;
        Suit suit = Suit.SPADES;
        
        // When
        Card card = new Card(rank, suit);
        
        // Then
        assertEquals(rank, card.getRank());
        assertEquals(suit, card.getSuit());
        assertEquals("A♠", card.getShortName());
        assertEquals("黑桃A", card.getChineseName());
    }
    
    @Test
    @DisplayName("建立撲克牌時點數不能為 null")
    void shouldThrowExceptionWhenRankIsNull() {
        // Given & When & Then
        assertThrows(NullPointerException.class, () -> {
            new Card(null, Suit.HEARTS);
        });
    }
    
    @Test
    @DisplayName("建立撲克牌時花色不能為 null")
    void shouldThrowExceptionWhenSuitIsNull() {
        // Given & When & Then
        assertThrows(NullPointerException.class, () -> {
            new Card(Rank.KING, null);
        });
    }
    
    @Test
    @DisplayName("應能正確比較撲克牌大小")
    void shouldCompareCardsCorrectly() {
        // Given
        Card aceSpades = new Card(Rank.ACE, Suit.SPADES);
        Card kingHearts = new Card(Rank.KING, Suit.HEARTS);
        Card aceHearts = new Card(Rank.ACE, Suit.HEARTS);
        
        // When & Then
        assertTrue(aceSpades.compareTo(kingHearts) > 0, "Ace should be greater than King");
        assertTrue(aceSpades.compareTo(aceHearts) > 0, "Spades should be greater than Hearts for same rank");
        assertEquals(0, aceSpades.compareTo(aceSpades), "Same card should be equal");
    }
    
    @Test
    @DisplayName("應能正確識別紅色和黑色牌")
    void shouldIdentifyRedAndBlackCards() {
        // Given
        Card redCard = new Card(Rank.QUEEN, Suit.HEARTS);
        Card blackCard = new Card(Rank.JACK, Suit.SPADES);
        
        // When & Then
        assertTrue(redCard.isRed());
        assertFalse(redCard.isBlack());
        assertTrue(blackCard.isBlack());
        assertFalse(blackCard.isRed());
    }
    
    @Test
    @DisplayName("應能從字符串正確創建撲克牌")
    void shouldCreateCardFromString() {
        // Given
        String cardString = "AS";
        
        // When
        Card card = Card.fromString(cardString);
        
        // Then
        assertEquals(Rank.ACE, card.getRank());
        assertEquals(Suit.SPADES, card.getSuit());
    }
    
    @Test
    @DisplayName("無效的字符串格式應拋出異常")
    void shouldThrowExceptionForInvalidString() {
        // Given & When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            Card.fromString("XY");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            Card.fromString("A");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            Card.fromString(null);
        });
    }
    
    @Test
    @DisplayName("equals 和 hashCode 應正確工作")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        // Given
        Card card1 = new Card(Rank.ACE, Suit.SPADES);
        Card card2 = new Card(Rank.ACE, Suit.SPADES);
        Card card3 = new Card(Rank.ACE, Suit.HEARTS);
        
        // When & Then
        assertEquals(card1, card2);
        assertNotEquals(card1, card3);
        assertEquals(card1.hashCode(), card2.hashCode());
    }
}