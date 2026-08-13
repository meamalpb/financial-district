package strategy_service.clients;

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
        String url = "http://ledger-service/internal/buy";

        BuyRequest request = new BuyRequest();
        request.setManId(decision.getManId());
        request.setSymbol(decision.getSymbol());
        request.setAmountToSpend(decision.getAmountToSpend());
        request.setPrice(decision.getPrice());

        return restTemplate.postForObject(url, request, LedgerAccountResponse.class);
    }
}
