package com.currencyconverter.currency_converter_backend.service;

import com.currencyconverter.currency_converter_backend.dto.AlertRequest;
import com.currencyconverter.currency_converter_backend.dto.AlertResponse;
import com.currencyconverter.currency_converter_backend.entity.Alert;
import com.currencyconverter.currency_converter_backend.entity.User;
import com.currencyconverter.currency_converter_backend.repository.AlertRepository;
import com.currencyconverter.currency_converter_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AlertService {
    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExchangeRateService exchangeRateService;

    /**
     * Create a new alert for a user
     */
    public AlertResponse createAlert(AlertRequest request, Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();

        // Validate condition type
        Alert.ConditionType conditionType;
        try {
            conditionType = Alert.ConditionType.valueOf(request.getConditionType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid condition type. Must be ABOVE or BELOW");
        }

        // Create new alert
        Alert alert = new Alert(
                user,
                request.getFromCurrency().toUpperCase(),
                request.getToCurrency().toUpperCase(),
                request.getTargetRate(),
                conditionType
        );

        Alert savedAlert = alertRepository.save(alert);
        logger.info("Created new alert: {} {} to {} at rate {}",
                conditionType, request.getFromCurrency(), request.getToCurrency(), request.getTargetRate());

        AlertResponse response = new AlertResponse(savedAlert);

        // Add current rate for reference
        BigDecimal currentRate = exchangeRateService.getExchangeRate(
                request.getFromCurrency(), request.getToCurrency()
        );
        response.setCurrentRate(currentRate);

        return response;
    }

    /**
     * Get all alerts for a user
     */
    public List<AlertResponse> getUserAlerts(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        List<Alert> alerts = alertRepository.findByUserOrderByCreatedAtDesc(userOpt.get());

        return alerts.stream()
                .map(alert -> {
                    AlertResponse response = new AlertResponse(alert);
                    // Add current rate for each alert
                    BigDecimal currentRate = exchangeRateService.getExchangeRate(
                            alert.getFromCurrencyCode(), alert.getToCurrencyCode()
                    );
                    response.setCurrentRate(currentRate);
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get active alerts for a user
     */
    public List<AlertResponse> getUserActiveAlerts(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        List<Alert> alerts = alertRepository.findByUserAndIsActiveTrueOrderByCreatedAtDesc(userOpt.get());

        return alerts.stream()
                .map(AlertResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get triggered alerts for a user
     */
    public List<AlertResponse> getUserTriggeredAlerts(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        List<Alert> alerts = alertRepository.findByUserAndIsTriggeredTrueOrderByTriggeredAtDesc(userOpt.get());

        return alerts.stream()
                .map(AlertResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Delete an alert
     */
    public boolean deleteAlert(Long alertId, Long userId) {
        Optional<Alert> alertOpt = alertRepository.findById(alertId);
        if (alertOpt.isEmpty()) {
            return false;
        }

        Alert alert = alertOpt.get();

        // Check if alert belongs to user
        if (!alert.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied: Alert does not belong to user");
        }

        alertRepository.delete(alert);
        logger.info("Deleted alert {} for user {}", alertId, userId);
        return true;
    }

    /**
     * Toggle alert active status
     */
    public AlertResponse toggleAlert(Long alertId, Long userId) {
        Optional<Alert> alertOpt = alertRepository.findById(alertId);
        if (alertOpt.isEmpty()) {
            throw new RuntimeException("Alert not found");
        }

        Alert alert = alertOpt.get();

        // Check if alert belongs to user
        if (!alert.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied: Alert does not belong to user");
        }

        alert.setIsActive(!alert.getIsActive());
        Alert savedAlert = alertRepository.save(alert);

        logger.info("Toggled alert {} to {}", alertId, alert.getIsActive() ? "active" : "inactive");

        return new AlertResponse(savedAlert);
    }

    /**
     * Check and trigger alerts (used by scheduler)
     */
    public void checkAndTriggerAlerts() {
        List<Alert> activeAlerts = alertRepository.findByIsActiveTrueAndIsTriggeredFalse();

        logger.info("Checking {} active alerts", activeAlerts.size());

        for (Alert alert : activeAlerts) {
            try {
                BigDecimal currentRate = exchangeRateService.getExchangeRate(
                        alert.getFromCurrencyCode(), alert.getToCurrencyCode()
                );

                if (currentRate != null && shouldTriggerAlert(alert, currentRate)) {
                    triggerAlert(alert, currentRate);
                }

            } catch (Exception e) {
                logger.error("Error checking alert {}: {}", alert.getId(), e.getMessage());
            }
        }
    }

    /**
     * Check if alert should be triggered based on current rate
     */
    private boolean shouldTriggerAlert(Alert alert, BigDecimal currentRate) {
        BigDecimal targetRate = alert.getTargetRate();
        Alert.ConditionType condition = alert.getConditionType();

        if (condition == Alert.ConditionType.ABOVE) {
            return currentRate.compareTo(targetRate) >= 0;
        } else { // BELOW
            return currentRate.compareTo(targetRate) <= 0;
        }
    }

    /**
     * Trigger an alert
     */
    private void triggerAlert(Alert alert, BigDecimal currentRate) {
        alert.setIsTriggered(true);
        alert.setTriggeredAt(LocalDateTime.now());
        alert.setIsActive(false); // Deactivate after triggering

        alertRepository.save(alert);

        logger.info("ALERT TRIGGERED! {} {} to {} - Target: {}, Current: {}",
                alert.getConditionType(),
                alert.getFromCurrencyCode(),
                alert.getToCurrencyCode(),
                alert.getTargetRate(),
                currentRate);

        // we can add more notification logic
        // For now, we just log and save to database
    }
}

