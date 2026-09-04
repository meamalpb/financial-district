package com.financialdistrict.strategy.services;

import java.math.BigDecimal;

// Builds a deterministic, human-readable manId out of a simulation's
// parameters (e.g. "drop-selling-man-aapl-inc10-tw10-sell10"). Same params
// always resolve to the same manId, which is what lets LedgerService detect
// "this exact simulation already ran" before replaying history a second time
// on top of an existing account.
final class ManIdSlug {

    private ManIdSlug() {
    }

    static String build(String prefix, String symbol, String... parts) {
        StringBuilder builder = new StringBuilder(prefix).append('-').append(symbol.toLowerCase());
        for (String part : parts) {
            builder.append('-').append(part);
        }
        return builder.toString();
    }

    static String number(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString().replace(".", "p").replace("-", "neg");
    }
}
