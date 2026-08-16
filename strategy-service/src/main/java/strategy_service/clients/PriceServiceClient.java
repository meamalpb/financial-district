package strategy_service.clients;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import strategy_service.dto.PriceBarResponse;
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

public List<PriceBarResponse> getHistory(String symbol, LocalDate from, LocalDate to) {
    String url = "http://price-service/internal/prices/" + symbol + "/history?from=" + from + "&to=" + to;
    return restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<PriceBarResponse>>() {})
            .getBody();
}

}