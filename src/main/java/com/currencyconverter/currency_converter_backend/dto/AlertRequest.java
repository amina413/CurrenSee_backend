package com.currencyconverter.currency_converter_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class AlertRequest {
    @NotBlank(message = "From currency code is required")
    private String fromCurrency;

    @NotBlank(message = "To currency code is required")
    private String toCurrency;

    @NotNull(message = "Target rate is required")
    @Positive(message = "Target rate must be positive")
    private BigDecimal targetRate;

    @NotBlank(message = "Condition type is required (ABOVE or BELOW)")
    private String conditionType; // "ABOVE" or "BELOW"

    // Constructors
    public AlertRequest() {}

    public AlertRequest(String fromCurrency, String toCurrency, BigDecimal targetRate, String conditionType) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.targetRate = targetRate;
        this.conditionType = conditionType;
    }

    // Getters and Setters
    public String getFromCurrency() { return fromCurrency; }
    public void setFromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; }

    public String getToCurrency() { return toCurrency; }
    public void setToCurrency(String toCurrency) { this.toCurrency = toCurrency; }

    public BigDecimal getTargetRate() { return targetRate; }
    public void setTargetRate(BigDecimal targetRate) { this.targetRate = targetRate; }

    public String getConditionType() { return conditionType; }
    public void setConditionType(String conditionType) { this.conditionType = conditionType; }
}
