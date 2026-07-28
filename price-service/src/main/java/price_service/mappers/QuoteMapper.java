package price_service.mappers;

import org.springframework.stereotype.Component;
import price_service.dto.QuoteResponse;
import price_service.models.Quote;

import java.time.LocalDateTime;

@Component
public class QuoteMapper {

    public Quote toEntity(QuoteResponse dto) {
        if (dto == null) {
            return null;
        }

        return Quote.builder()
                .symbol(dto.symbol())
                .name(dto.name())
                .exchange(dto.exchange())
                .currency(dto.currency())
                .datetime(dto.datetime())
                .timestamp(dto.timestamp())
                .open(dto.open())
                .high(dto.high())
                .low(dto.low())
                .close(dto.close())
                .volume(dto.volume())
                .previousClose(dto.previousClose())
                .change(dto.change())
                .percentChange(dto.percentChange())
                .averageVolume(dto.averageVolume())
                .isMarketOpen(dto.isMarketOpen())
                .fiftyTwoWeek(toFiftyTwoWeekStats(dto.fiftyTwoWeek()))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Quote.FiftyTwoWeekStats toFiftyTwoWeekStats(QuoteResponse.FiftyTwoWeek dto) {
        if (dto == null) {
            return null;
        }

        return Quote.FiftyTwoWeekStats.builder()
                .low(dto.low())
                .high(dto.high())
                .lowChange(dto.lowChange())
                .highChange(dto.highChange())
                .lowChangePercent(dto.lowChangePercent())
                .highChangePercent(dto.highChangePercent())
                .range(dto.range())
                .build();
    }
}