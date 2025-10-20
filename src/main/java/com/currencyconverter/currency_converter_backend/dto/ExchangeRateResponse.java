package com.currencyconverter.currency_converter_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Map;

public class ExchangeRateResponse {
    @JsonProperty("result")
    private String result;

    @JsonProperty("base_code")
    private String baseCode;

    @JsonProperty("target_code")
    private String targetCode;

    @JsonProperty("conversion_rate")
    private BigDecimal conversionRate;

    @JsonProperty("conversion_rates")
    private Map<String, BigDecimal> conversionRates;

    @JsonProperty("time_last_update_unix")
    private Long timeLastUpdate;

    // Constructors
    public ExchangeRateResponse() {}

    // Getters and Setters
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getBaseCode() { return baseCode; }
    public void setBaseCode(String baseCode) { this.baseCode = baseCode; }

    public String getTargetCode() { return targetCode; }
    public void setTargetCode(String targetCode) { this.targetCode = targetCode; }

    public BigDecimal getConversionRate() { return conversionRate; }
    public void setConversionRate(BigDecimal conversionRate) { this.conversionRate = conversionRate; }

    public Map<String, BigDecimal> getConversionRates() { return conversionRates; }
    public void setConversionRates(Map<String, BigDecimal> conversionRates) { this.conversionRates = conversionRates; }

    public Long getTimeLastUpdate() { return timeLastUpdate; }
    public void setTimeLastUpdate(Long timeLastUpdate) { this.timeLastUpdate = timeLastUpdate; }
}
