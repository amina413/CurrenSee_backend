package com.currencyconverter.currency_converter_backend.repository;

import com.currencyconverter.currency_converter_backend.entity.Alert;
import com.currencyconverter.currency_converter_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    // Find alerts by user
    List<Alert> findByUserOrderByCreatedAtDesc(User user);

    // Find active alerts by user
    List<Alert> findByUserAndIsActiveTrueOrderByCreatedAtDesc(User user);

    // Find all active alerts (for background monitoring)
    List<Alert> findByIsActiveTrueAndIsTriggeredFalse();

    // Find alerts by currency pair
    List<Alert> findByFromCurrencyCodeAndToCurrencyCode(String fromCurrency, String toCurrency);

    // Find triggered alerts for a user
    List<Alert> findByUserAndIsTriggeredTrueOrderByTriggeredAtDesc(User user);

    // Count active alerts for a user
    long countByUserAndIsActiveTrueAndIsTriggeredFalse(User user);

    // Find alerts by user and currency pair
    @Query("SELECT a FROM Alert a WHERE a.user = :user AND a.fromCurrencyCode = :fromCurrency AND a.toCurrencyCode = :toCurrency AND a.isActive = true")
    List<Alert> findActiveAlertsByUserAndCurrencyPair(User user, String fromCurrency, String toCurrency);
}
