package com.currencyconverter.currency_converter_backend.controller;

import com.currencyconverter.currency_converter_backend.dto.ConversionRequest;
import com.currencyconverter.currency_converter_backend.dto.ConversionResponse;
import com.currencyconverter.currency_converter_backend.dto.MessageResponse;
import com.currencyconverter.currency_converter_backend.entity.Conversion;
import com.currencyconverter.currency_converter_backend.entity.Currency;
import com.currencyconverter.currency_converter_backend.service.CurrencyService;
import com.currencyconverter.currency_converter_backend.service.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.currencyconverter.currency_converter_backend.service.ExchangeRateService;
import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/currency")
public class CurrencyController {

    @Autowired
    private CurrencyService currencyService;

    /**
     * Get all active currencies
     */
    @GetMapping("/list")
    public ResponseEntity<List<Currency>> getAllCurrencies() {
        List<Currency> currencies = currencyService.getAllActiveCurrencies();
        return ResponseEntity.ok(currencies);
    }

    /**
     * Search currencies by name
     */
    @GetMapping("/search")
    public ResponseEntity<List<Currency>> searchCurrencies(@RequestParam String name) {
        List<Currency> currencies = currencyService.searchCurrencies(name);
        return ResponseEntity.ok(currencies);
    }

    /**
     * Get currency by code
     */
    @GetMapping("/{code}")
    public ResponseEntity<Currency> getCurrencyByCode(@PathVariable String code) {
        Optional<Currency> currency = currencyService.getCurrencyByCode(code);
        if (currency.isPresent()) {
            return ResponseEntity.ok(currency.get());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Convert currency (public endpoint - no authentication required)
     */
    @PostMapping("/convert")
    public ResponseEntity<?> convertCurrency(@Valid @RequestBody ConversionRequest request) {
        try {
            ConversionResponse response = currencyService.convertCurrency(request, null, false);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Convert currency and save to history (authenticated endpoint)
     */
    @PostMapping("/convert-and-save")
    public ResponseEntity<?> convertAndSaveCurrency(@Valid @RequestBody ConversionRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            Long userId = null;
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetailsImpl) {
                userId = ((UserDetailsImpl) principal).getId();
            } else if (principal instanceof String) {
                // Get user ID from username
                String username = (String) principal;
                userId = currencyService.getUserIdByUsername(username);
                if (userId == null) {
                    return ResponseEntity.badRequest()
                            .body(new MessageResponse("User not found"));
                }
            }

            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Authentication error: Unable to get user ID"));
            }

            ConversionResponse response = currencyService.convertCurrency(request, userId, true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    /**
     * Get user's conversion history
     */
    @GetMapping("/history")
    public ResponseEntity<Page<Conversion>> getConversionHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        Pageable pageable = PageRequest.of(page, size);
        Page<Conversion> conversions = currencyService.getUserConversionHistory(userId, pageable);

        return ResponseEntity.ok(conversions);
    }

    /**
     * Get recent conversions for dashboard
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Conversion>> getRecentConversions(
            @RequestParam(defaultValue = "5") int limit) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        List<Conversion> conversions = currencyService.getRecentConversions(userId, limit);
        return ResponseEntity.ok(conversions);
    }

    /**
     * Test external API connection
     */
    @GetMapping("/test-api")
    public ResponseEntity<?> testExternalApi() {
        try {
            String testUrl = "https://api.exchangerate-api.com/v4/latest/USD";
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(testUrl, String.class);
            return ResponseEntity.ok("API Response: " + response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("API Error: " + e.getMessage()));
        }
    }

    @Autowired
    private ExchangeRateService exchangeRateService;

    /**
     * Debug exchange rate service
     */
    @GetMapping("/debug-rate")
    public ResponseEntity<?> debugExchangeRate() {
        try {
            BigDecimal rate = exchangeRateService.getExchangeRate("USD", "EUR");
            if (rate != null) {
                return ResponseEntity.ok("Exchange Rate USD to EUR: " + rate);
            } else {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Rate is null - check logs for details"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Debug Error: " + e.getMessage()));
        }
    }
}
