package com.currencyconverter.currency_converter_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "from_currency_code", nullable = false, length = 3)
    @NotBlank(message = "From currency code is required")
    private String fromCurrencyCode;

    @Column(name = "to_currency_code", nullable = false, length = 3)
    @NotBlank(message = "To currency code is required")
    private String toCurrencyCode;

    @Column(name = "target_rate", nullable = false, precision = 10, scale = 6)
    @NotNull(message = "Target rate is required")
    @Positive(message = "Target rate must be positive")
    private BigDecimal targetRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false)
    private ConditionType conditionType;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_triggered")
    private Boolean isTriggered = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "triggered_at")
    private LocalDateTime triggeredAt;

    // Enum for condition types
    public enum ConditionType {
        ABOVE, BELOW
    }

    // Constructors
    public Alert() {}

    public Alert(User user, String fromCurrencyCode, String toCurrencyCode,
                 BigDecimal targetRate, ConditionType conditionType) {
        this.user = user;
        this.fromCurrencyCode = fromCurrencyCode;
        this.toCurrencyCode = toCurrencyCode;
        this.targetRate = targetRate;
        this.conditionType = conditionType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getFromCurrencyCode() { return fromCurrencyCode; }
    public void setFromCurrencyCode(String fromCurrencyCode) { this.fromCurrencyCode = fromCurrencyCode; }

    public String getToCurrencyCode() { return toCurrencyCode; }
    public void setToCurrencyCode(String toCurrencyCode) { this.toCurrencyCode = toCurrencyCode; }

    public BigDecimal getTargetRate() { return targetRate; }
    public void setTargetRate(BigDecimal targetRate) { this.targetRate = targetRate; }

    public ConditionType getConditionType() { return conditionType; }
    public void setConditionType(ConditionType conditionType) { this.conditionType = conditionType; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsTriggered() { return isTriggered; }
    public void setIsTriggered(Boolean isTriggered) { this.isTriggered = isTriggered; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(LocalDateTime triggeredAt) { this.triggeredAt = triggeredAt; }
}
