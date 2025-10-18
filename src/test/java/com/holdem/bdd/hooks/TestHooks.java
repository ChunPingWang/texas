package com.holdem.bdd.hooks;

import com.holdem.bdd.steps.CommonSteps;
import io.cucumber.java.Before;

/**
 * Cucumber 測試 Hooks
 * 在測試執行前後進行必要的設定和清理
 * 
 * @author Development Team
 * @version 1.0
 * @since 2025-10-18
 */
public class TestHooks {

    @Before
    public void beforeEachScenario() {
        // 每個情境開始前重設共用狀態
        CommonSteps.reset();
    }
}