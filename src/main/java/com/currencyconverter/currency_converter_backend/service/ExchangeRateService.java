package com.currencyconverter.currency_converter_backend.service;

import com.currencyconverter.currency_converter_backend.dto.ExchangeRateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;


import java.math.BigDecimal;
import java.util.Map;

@Service
public class ExchangeRateService {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateService.class);

    @Value("${app.exchangerate.api.url:https://api.exchangerate-api.com/v4/latest}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public ExchangeRateService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Get exchange rate between two currencies
     */
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        try {
            String url = apiUrl + "/" + fromCurrency.toUpperCase();

            // Get response as Map to see the actual structure
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("rates")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> rates = (Map<String, Object>) response.get("rates");

                if (rates != null && rates.containsKey(toCurrency.toUpperCase())) {
                    Object rateValue = rates.get(toCurrency.toUpperCase());
                    return new BigDecimal(rateValue.toString());
                }
            }

            logger.error("Failed to get exchange rate for {} to {}. Response: {}",
                    fromCurrency, toCurrency, response);
            return null;

        } catch (RestClientException e) {
            logger.error("Error calling exchange rate API: {}", e.getMessage());
            return null;
        }
    }
    /**
     * Get all exchange rates for a base currency
     */
    public Map<String, BigDecimal> getAllRates(String baseCurrency) {
        try {
            String url = apiUrl + "/" + baseCurrency.toUpperCase();
            ExchangeRateResponse response = restTemplate.getForObject(url, ExchangeRateResponse.class);

            if (response != null) {
                return response.getConversionRates();
            }

            logger.error("Failed to get rates for base currency: {}", baseCurrency);
            return null;

        } catch (RestClientException e) {
            logger.error("Error calling exchange rate API: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Convert amount from one currency to another
     */
    public BigDecimal convertAmount(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return amount;
        }

        BigDecimal rate = getExchangeRate(fromCurrency, toCurrency);
        if (rate != null) {
            return amount.multiply(rate);
        }

        return null;
    }
}

