package com.financialdistrict.price.providers;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import com.financialdistrict.price.dto.QuoteResponse;
import com.financialdistrict.price.dto.TwelveDataPriceResponse;
import com.financialdistrict.price.dto.TwelveDataTimeSeriesResponse;


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

    public QuoteResponse getQuote(String symbol) {
        String url = BaseUrl + "/quote?symbol=" + symbol + "&apikey="+ apikey;
        return restTemplate.getForObject(url, QuoteResponse.class);
    }

    public TwelveDataTimeSeriesResponse getDailyHistory(String symbol) {
        String url = BaseUrl + "/time_series?symbol=" + symbol + "&interval=1day&outputsize=5000&apikey=" + apikey;
        return restTemplate.getForObject(url, TwelveDataTimeSeriesResponse.class);
    }

    public TwelveDataTimeSeriesResponse getDailyHistory(String symbol, LocalDate endDate) {
        String url = BaseUrl + "/time_series?symbol=" + symbol + "&interval=1day&outputsize=5000&end_date=" + endDate + "&apikey=" + apikey;
        return restTemplate.getForObject(url, TwelveDataTimeSeriesResponse.class);
    }

}
