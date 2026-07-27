package price_service.providers;

import org.springframework.beans.factory.annotation.Value;

// import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

// import price_service.dto.TempLatestPriceDTO;

@Service
public class TwelveDataProvider {

    @Value("${twelvedata.baseurl}")
    public String BaseUrl;

    @Value("${twelvedata.apikey}")
    public String apikey;
    
    private final RestTemplate restTemplate = new RestTemplate();

    public String getLatestPrice(String symbol) {
        String url = BaseUrl + "/price?symbol=" + symbol + "&apikey="+ apikey;
        return restTemplate.getForObject(url, String.class);
    }

}
