package com.currencyconverter.currency_converter_backend.service;

import com.currencyconverter.currency_converter_backend.dto.ConversionRequest;
import com.currencyconverter.currency_converter_backend.dto.ConversionResponse;
import com.currencyconverter.currency_converter_backend.entity.Conversion;
import com.currencyconverter.currency_converter_backend.entity.Currency;
import com.currencyconverter.currency_converter_backend.entity.User;
import com.currencyconverter.currency_converter_backend.repository.ConversionRepository;
import com.currencyconverter.currency_converter_backend.repository.CurrencyRepository;
import com.currencyconverter.currency_converter_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class CurrencyService {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private ConversionRepository conversionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExchangeRateService exchangeRateService;

    /**
     * Get user ID by username
     */
    public Long getUserIdByUsername(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        return userOpt.map(User::getId).orElse(null);
    }

    /**
     * Get all active currencies
     */
    public List<Currency> getAllActiveCurrencies() {
        return currencyRepository.findByIsActiveTrue();
    }

    /**
     * Get currency by code
     */
    public Optional<Currency> getCurrencyByCode(String code) {
        return currencyRepository.findByCode(code.toUpperCase());
    }

    /**
     * Add or update currency
     */
    public Currency saveCurrency(Currency currency) {
        currency.setCode(currency.getCode().toUpperCase());
        return currencyRepository.save(currency);
    }

    /**
     * Search currencies by name
     */
    public List<Currency> searchCurrencies(String name) {
        return currencyRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Convert currency and optionally save to history
     */
    public ConversionResponse convertCurrency(ConversionRequest request, Long userId, boolean saveToHistory) {
        String fromCurrency = request.getFromCurrency().toUpperCase();
        String toCurrency = request.getToCurrency().toUpperCase();

        // Get exchange rate
        BigDecimal exchangeRate = exchangeRateService.getExchangeRate(fromCurrency, toCurrency);
        if (exchangeRate == null) {
            throw new RuntimeException("Unable to get exchange rate for " + fromCurrency + " to " + toCurrency);
        }

        // Calculate converted amount
        BigDecimal convertedAmount = request.getAmount().multiply(exchangeRate)
                .setScale(6, RoundingMode.HALF_UP);

        ConversionResponse response = new ConversionResponse(
                fromCurrency, toCurrency, request.getAmount(), convertedAmount, exchangeRate
        );

        // Save to history if requested and user is authenticated
        if (saveToHistory && userId != null) {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                Conversion conversion = new Conversion(
                        userOpt.get(), fromCurrency, toCurrency,
                        request.getAmount(), convertedAmount, exchangeRate
                );
                conversionRepository.save(conversion);
                response.setSaved(true);
            }
        }

        return response;
    }

    /**
     * Get user's conversion history
     */
    public Page<Conversion> getUserConversionHistory(Long userId, Pageable pageable) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            return conversionRepository.findByUserOrderByConversionDateDesc(userOpt.get(), pageable);
        }
        return Page.empty();
    }

    /**
     * Get recent conversions for a user
     */
    public List<Conversion> getRecentConversions(Long userId, int limit) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            return conversionRepository.findRecentConversions(userOpt.get(),
                    org.springframework.data.domain.PageRequest.of(0, limit));
        }
        return List.of();
    }
}
