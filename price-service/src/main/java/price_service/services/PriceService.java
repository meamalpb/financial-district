package price_service.services;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import price_service.dto.QuoteResponse;
import price_service.mappers.QuoteMapper;
import price_service.models.Quote;
import price_service.providers.TwelveDataProvider;
import price_service.repositories.QuoteRepository;

@RequiredArgsConstructor
@Service
public class PriceService {
    private final TwelveDataProvider twelveDataProvider;
    private final QuoteRepository quoteRepository;
    private final QuoteMapper quoteMapper;


    public BigDecimal twelveDataGetCurrentPrice(String symbol){
        return twelveDataProvider.getCurrentPrice(symbol);
    }

    public Quote twelveDataGetQuote(String symbol){
        QuoteResponse quoteResponse =  twelveDataProvider.getQuote(symbol);
        Quote quote = quoteMapper.toEntity(quoteResponse);
        return quoteRepository.save(quote);
    }
    
}
