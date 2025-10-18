package com.holdem.bdd.steps;

import io.cucumber.java.zh_tw.那麼;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 通用的步驟定義類別
 * 包含所有測試場景共用的步驟
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
@Component
public class CommonSteps {

    // 使用靜態變數來共用錯誤訊息，這樣所有步驟定義類別都可以存取
    private static String actualErrorMessage;
    private static Exception lastException;

    /**
     * 設定實際的錯誤訊息
     * 供其他步驟定義類別使用
     */
    public static void setActualErrorMessage(String message) {
        actualErrorMessage = message;
    }

    /**
     * 設定最後的異常
     * 供其他步驟定義類別使用
     */
    public static void setLastException(Exception exception) {
        lastException = exception;
        if (exception != null) {
            actualErrorMessage = exception.getMessage();
        }
    }

    /**
     * 取得實際的錯誤訊息
     * 供其他步驟定義類別使用
     */
    public static String getActualErrorMessage() {
        return actualErrorMessage;
    }

    @那麼("系統應該顯示錯誤訊息 {string}")
    public void 系統應該顯示錯誤訊息(String expectedMessage) {
        // 檢查異常或錯誤訊息
        if (lastException != null) {
            assertTrue(lastException.getMessage().contains("牌組不足") || 
                      lastException.getMessage().contains("insufficient") ||
                      lastException.getMessage().contains(expectedMessage), 
                      String.format("錯誤訊息應該包含相關內容，但實際是 '%s'", lastException.getMessage()));
        } else if (actualErrorMessage != null) {
            assertTrue(actualErrorMessage.contains("有效的下注金額") || 
                      actualErrorMessage.contains(expectedMessage), 
                      String.format("錯誤訊息應該包含 '%s'，但實際是 '%s'", expectedMessage, actualErrorMessage));
        } else {
            fail("應該有錯誤訊息或異常");
        }
    }

    /**
     * 清除所有狀態，供測試重設使用
     */
    public static void reset() {
        actualErrorMessage = null;
        lastException = null;
    }
}