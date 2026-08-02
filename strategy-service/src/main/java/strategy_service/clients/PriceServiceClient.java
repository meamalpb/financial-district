package strategy_service.clients;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import strategy_service.dto.PriceResponse;

@Component
public class PriceServiceClient {

    private final RestTemplate restTemplate;

    public PriceServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

public BigDecimal getCurrentPrice(String symbol) {
    String url = "http://price-service/internal/prices/" + symbol;
    PriceResponse response = restTemplate.getForObject(url, PriceResponse.class);
    return response.getClose();
}

}