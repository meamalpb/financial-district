package strategy_service.strategies;

import java.math.BigDecimal;

import strategy_service.events.BuyDecision;
import strategy_service.models.ManConfig;

public interface Strategy {
    BuyDecision decide(ManConfig manConfig, BigDecimal currentPrice);
}