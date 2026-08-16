package strategy_service.controllers;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import strategy_service.dto.SimulationResult;
import strategy_service.events.BuyDecision;
import strategy_service.services.StrategyEngineService;

@RestController
@RequiredArgsConstructor
public class StrategyController {

    private final StrategyEngineService strategyEngineService;

    @GetMapping("/internal/run-temp-man")
    public BuyDecision runTempMan() {
        return strategyEngineService.runCycleForTempMan();
    }

    @GetMapping("/internal/simulate-temp-man")
    public SimulationResult simulateTempMan(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return strategyEngineService.simulateForTempMan(from, to);
    }
}