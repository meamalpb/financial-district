package price_service.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import price_service.dto.TwelveDataTimeSeriesResponse;
import price_service.mappers.PriceHistoryMapper;
import price_service.models.PriceHistory;
import price_service.providers.TwelveDataProvider;
import price_service.repositories.PriceHistoryRepository;

@Service
@RequiredArgsConstructor
public class PriceHistoryService {

    private static final int PAGE_SIZE = 5000;

    private final TwelveDataProvider twelveDataProvider;
    private final PriceHistoryMapper priceHistoryMapper;
    private final PriceHistoryRepository priceHistoryRepository;

    public int backfill(String symbol) {
        int savedCount = 0;
        LocalDate endDate = null;

        while (true) {
            TwelveDataTimeSeriesResponse response = endDate == null
                    ? twelveDataProvider.getDailyHistory(symbol)
                    : twelveDataProvider.getDailyHistory(symbol, endDate);

            List<PriceHistory> bars = priceHistoryMapper.toEntities(symbol, response);
            if (bars.isEmpty()) {
                break;
            }

            for (PriceHistory bar : bars) {
                if (priceHistoryRepository.findBySymbolAndDate(symbol, bar.getDate()).isEmpty()) {
                    priceHistoryRepository.save(bar);
                    savedCount++;
                }
            }

            LocalDate oldestInBatch = bars.get(bars.size() - 1).getDate();
            if (bars.size() < PAGE_SIZE || oldestInBatch.equals(endDate)) {
                break;
            }
            endDate = oldestInBatch;
        }

        return savedCount;
    }
}
