package strategy_service.clients;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import strategy_service.dto.BuyRequest;
import strategy_service.dto.LedgerAccountResponse;
import strategy_service.events.BuyDecision;

@Component
public class LedgerServiceClient {

    private final RestTemplate restTemplate;

    public LedgerServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public LedgerAccountResponse applyBuy(BuyDecision decision) {
        return applyBuy(decision, null);
    }

    // timestamp dates the transaction to a historical price bar for simulated buys;
    // pass null for live buys so LedgerService stamps it with the real current time.
    public LedgerAccountResponse applyBuy(BuyDecision decision, LocalDateTime timestamp) {
        String url = "http://ledger-service/internal/buy";

        BuyRequest request = new BuyRequest();
        request.setManId(decision.getManId());
        request.setSymbol(decision.getSymbol());
        request.setAmountToSpend(decision.getAmountToSpend());
        request.setPrice(decision.getPrice());
        request.setTimestamp(timestamp);

        return restTemplate.postForObject(url, request, LedgerAccountResponse.class);
    }
}
