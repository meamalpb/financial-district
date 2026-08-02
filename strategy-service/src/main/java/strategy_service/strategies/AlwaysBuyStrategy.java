package strategy_service.strategies;

import java.math.BigDecimal;

import strategy_service.events.BuyDecision;
import strategy_service.models.ManConfig;

public class AlwaysBuyStrategy implements Strategy {

    @Override
    public BuyDecision decide(ManConfig manConfig, BigDecimal currentPrice) {
        BuyDecision decision = new BuyDecision();
        decision.setManId(manConfig.getManId());
        decision.setSymbol(manConfig.getSymbols().get(0)); // temp: first symbol only, for now
        decision.setPrice(currentPrice);

        BigDecimal balance = manConfig.getCurrentBalance();
        if (balance == null || balance.compareTo(BigDecimal.ZERO) <= 0) {
            decision.setShouldBuy(false);
            decision.setAmountToSpend(BigDecimal.ZERO);
        } else {
            decision.setShouldBuy(true);
            decision.setAmountToSpend(balance);
        }

        return decision;
    }
}