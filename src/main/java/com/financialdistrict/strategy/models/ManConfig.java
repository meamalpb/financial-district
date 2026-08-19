package com.financialdistrict.strategy.models;


import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class ManConfig {

    private String manId;
    private List<String> symbols;
    private String strategyName;
    private boolean active;
    private BigDecimal currentBalance;

}
