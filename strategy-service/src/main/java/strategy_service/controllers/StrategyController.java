package strategy_service.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
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
}