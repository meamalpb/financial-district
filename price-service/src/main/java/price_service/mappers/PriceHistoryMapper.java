package price_service.mappers;

import org.springframework.stereotype.Component;
import price_service.dto.TwelveDataTimeSeriesResponse;
import price_service.models.PriceHistory;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PriceHistoryMapper {

    public List<PriceHistory> toEntities(String symbol, TwelveDataTimeSeriesResponse dto) {
        if (dto == null || dto.values() == null) {
            return List.of();
        }

        return dto.values().stream()
                .map(bar -> toEntity(symbol, bar))
                .toList();
    }

    private PriceHistory toEntity(String symbol, TwelveDataTimeSeriesResponse.Bar bar) {
        return PriceHistory.builder()
                .symbol(symbol)
                .date(bar.datetime())
                .open(bar.open())
                .high(bar.high())
                .low(bar.low())
                .close(bar.close())
                .volume(bar.volume())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
