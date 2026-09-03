package com.financialdistrict.price.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import com.financialdistrict.price.dto.PriceHistorySummaryResponse;
import com.financialdistrict.price.dto.TwelveDataTimeSeriesResponse;
import com.financialdistrict.price.mappers.PriceHistoryMapper;
import com.financialdistrict.price.models.PriceHistory;
import com.financialdistrict.price.providers.TwelveDataProvider;
import com.financialdistrict.price.repositories.PriceHistoryRepository;

@Service
@RequiredArgsConstructor
public class PriceHistoryService {

    private static final int PAGE_SIZE = 5000;
    private static final int BATCH_SIZE = 500;

    private static final String INSERT_SQL = """
            INSERT INTO price_history (symbol, date, open, high, low, close, volume, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (symbol, date) DO NOTHING
            """;

    private final TwelveDataProvider twelveDataProvider;
    private final PriceHistoryMapper priceHistoryMapper;
    private final JdbcTemplate jdbcTemplate;
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

            savedCount += saveBatch(bars);

            LocalDate oldestInBatch = bars.get(bars.size() - 1).getDate();
            if (bars.size() < PAGE_SIZE || oldestInBatch.equals(endDate)) {
                break;
            }
            endDate = oldestInBatch;
        }

        return savedCount;
    }

    public List<PriceHistory> getHistory(String symbol) {
        return priceHistoryRepository.findBySymbolOrderByDateAsc(symbol);
    }

    public List<PriceHistorySummaryResponse> getSummaries() {
        return priceHistoryRepository.getSummaries();
    }

    @Transactional
    protected int saveBatch(List<PriceHistory> bars) {
        int[][] results = jdbcTemplate.batchUpdate(INSERT_SQL, bars, BATCH_SIZE, (ps, bar) -> {
            ps.setString(1, bar.getSymbol());
            ps.setObject(2, bar.getDate());
            ps.setBigDecimal(3, bar.getOpen());
            ps.setBigDecimal(4, bar.getHigh());
            ps.setBigDecimal(5, bar.getLow());
            ps.setBigDecimal(6, bar.getClose());
            ps.setLong(7, bar.getVolume());
            ps.setObject(8, LocalDateTime.now());
        });

        int inserted = 0;
        for (int[] batch : results) {
            for (int result : batch) {
                if (result > 0) {
                    inserted += result;
                } else if (result == java.sql.Statement.SUCCESS_NO_INFO) {
                    inserted += 1;
                }
            }
        }
        return inserted;
    }
}
