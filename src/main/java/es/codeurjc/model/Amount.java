package es.codeurjc.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Amount {
    private final BigDecimal value;

    public Amount(BigDecimal value) {
        this.value = value.setScale(2, RoundingMode.HALF_EVEN);
    }

    
    public static Amount fromDouble(double value) {
        return new Amount(BigDecimal.valueOf(value));
    }

    public BigDecimal getValue() {
        return value;
    }

    public boolean isGreaterThan(Amount otherAmount) {
        return this.value.compareTo(otherAmount.value) > 0;
    }

    public boolean isPositive() {
        return this.value.compareTo(BigDecimal.ZERO) > 0;
    }

}