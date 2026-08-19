package com.financialdistrict.strategy.strategies;

import java.math.BigDecimal;

import com.financialdistrict.strategy.events.BuyDecision;
import com.financialdistrict.strategy.models.ManConfig;

public interface Strategy {
    BuyDecision decide(ManConfig manConfig, BigDecimal currentPrice);
}
