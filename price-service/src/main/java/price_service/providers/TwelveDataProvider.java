package price_service.providers;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import price_service.dto.TwelveDataPriceResponse;


@Service
@RequiredArgsConstructor
public class TwelveDataProvider implements PriceProvider {

    @Value("${twelvedata.baseurl}")
    private String BaseUrl;

    @Value("${twelvedata.apikey}")
    private String apikey;
    
    private final RestTemplate restTemplate;

    @Override
    public BigDecimal getCurrentPrice(String symbol) {
        String url = BaseUrl + "/price?symbol=" + symbol + "&apikey="+ apikey;
        return restTemplate.getForObject(url, TwelveDataPriceResponse.class).price();
    }

}
