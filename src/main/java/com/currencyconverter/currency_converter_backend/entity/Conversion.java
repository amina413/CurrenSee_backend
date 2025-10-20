package com.currencyconverter.currency_converter_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversions")
public class Conversion {
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

    @Column(nullable = false, precision = 15, scale = 6)
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @Column(name = "converted_amount", nullable = false, precision = 15, scale = 6)
    @NotNull(message = "Converted amount is required")
    private BigDecimal convertedAmount;

    @Column(name = "exchange_rate", nullable = false, precision = 10, scale = 6)
    @NotNull(message = "Exchange rate is required")
    @Positive(message = "Exchange rate must be positive")
    private BigDecimal exchangeRate;

    @CreationTimestamp
    @Column(name = "conversion_date", updatable = false)
    private LocalDateTime conversionDate;

    // Constructors
    public Conversion() {}

    public Conversion(User user, String fromCurrencyCode, String toCurrencyCode,
                      BigDecimal amount, BigDecimal convertedAmount, BigDecimal exchangeRate) {
        this.user = user;
        this.fromCurrencyCode = fromCurrencyCode;
        this.toCurrencyCode = toCurrencyCode;
        this.amount = amount;
        this.convertedAmount = convertedAmount;
        this.exchangeRate = exchangeRate;
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

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getConvertedAmount() { return convertedAmount; }
    public void setConvertedAmount(BigDecimal convertedAmount) { this.convertedAmount = convertedAmount; }

    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }

    public LocalDateTime getConversionDate() { return conversionDate; }
    public void setConversionDate(LocalDateTime conversionDate) { this.conversionDate = conversionDate; }
}
