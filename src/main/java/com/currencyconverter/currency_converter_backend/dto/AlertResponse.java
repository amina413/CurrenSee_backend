package com.currencyconverter.currency_converter_backend.dto;

import com.currencyconverter.currency_converter_backend.entity.Alert;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AlertResponse {
    private Long id;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal targetRate;
    private String conditionType;
    private boolean isActive;
    private boolean isTriggered;
    private LocalDateTime createdAt;
    private LocalDateTime triggeredAt;
    private BigDecimal currentRate;

    // Constructor from Alert entity
    public AlertResponse(Alert alert) {
        this.id = alert.getId();
        this.fromCurrency = alert.getFromCurrencyCode();
        this.toCurrency = alert.getToCurrencyCode();
        this.targetRate = alert.getTargetRate();
        this.conditionType = alert.getConditionType().name();
        this.isActive = alert.getIsActive();
        this.isTriggered = alert.getIsTriggered();
        this.createdAt = alert.getCreatedAt();
        this.triggeredAt = alert.getTriggeredAt();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFromCurrency() { return fromCurrency; }
    public void setFromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; }

    public String getToCurrency() { return toCurrency; }
    public void setToCurrency(String toCurrency) { this.toCurrency = toCurrency; }

    public BigDecimal getTargetRate() { return targetRate; }
    public void setTargetRate(BigDecimal targetRate) { this.targetRate = targetRate; }

    public String getConditionType() { return conditionType; }
    public void setConditionType(String conditionType) { this.conditionType = conditionType; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isTriggered() { return isTriggered; }
    public void setTriggered(boolean triggered) { isTriggered = triggered; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(LocalDateTime triggeredAt) { this.triggeredAt = triggeredAt; }

    public BigDecimal getCurrentRate() { return currentRate; }
    public void setCurrentRate(BigDecimal currentRate) { this.currentRate = currentRate; }
}
