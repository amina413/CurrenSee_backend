package com.currencyconverter.currency_converter_backend.controller;

import com.currencyconverter.currency_converter_backend.dto.AlertRequest;
import com.currencyconverter.currency_converter_backend.dto.AlertResponse;
import com.currencyconverter.currency_converter_backend.dto.MessageResponse;
import com.currencyconverter.currency_converter_backend.service.AlertService;
import com.currencyconverter.currency_converter_backend.service.CurrencyService;
import com.currencyconverter.currency_converter_backend.service.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @Autowired
    private CurrencyService currencyService;

    /**
     * Create a new alert
     */
    @PostMapping
    public ResponseEntity<?> createAlert(@Valid @RequestBody AlertRequest request) {
        try {
            Long userId = getUserId();
            AlertResponse response = alertService.createAlert(request, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get all alerts for user
     */
    @GetMapping
    public ResponseEntity<List<AlertResponse>> getUserAlerts() {
        Long userId = getUserId();
        List<AlertResponse> alerts = alertService.getUserAlerts(userId);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get active alerts for user
     */
    @GetMapping("/active")
    public ResponseEntity<List<AlertResponse>> getActiveAlerts() {
        Long userId = getUserId();
        List<AlertResponse> alerts = alertService.getUserActiveAlerts(userId);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get triggered alerts for user
     */
    @GetMapping("/triggered")
    public ResponseEntity<List<AlertResponse>> getTriggeredAlerts() {
        Long userId = getUserId();
        List<AlertResponse> alerts = alertService.getUserTriggeredAlerts(userId);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Delete an alert
     */
    @DeleteMapping("/{alertId}")
    public ResponseEntity<?> deleteAlert(@PathVariable Long alertId) {
        try {
            Long userId = getUserId();
            boolean deleted = alertService.deleteAlert(alertId, userId);
            if (deleted) {
                return ResponseEntity.ok(new MessageResponse("Alert deleted successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Toggle alert active/inactive
     */
    @PutMapping("/{alertId}/toggle")
    public ResponseEntity<?> toggleAlert(@PathVariable Long alertId) {
        try {
            Long userId = getUserId();
            AlertResponse response = alertService.toggleAlert(alertId, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Manually trigger alert check (for testing)
     */
    @PostMapping("/check")
    public ResponseEntity<?> checkAlerts() {
        try {
            alertService.checkAndTriggerAlerts();
            return ResponseEntity.ok(new MessageResponse("Alert check completed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Helper method to get user ID from authentication
     */
    private Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) principal).getId();
        } else if (principal instanceof String) {
            String username = (String) principal;
            return currencyService.getUserIdByUsername(username);
        }

        throw new RuntimeException("Authentication error: Unable to get user ID");
    }
}
