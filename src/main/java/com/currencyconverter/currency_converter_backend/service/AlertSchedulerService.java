package com.currencyconverter.currency_converter_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AlertSchedulerService {
    private static final Logger logger = LoggerFactory.getLogger(AlertSchedulerService.class);

    @Autowired
    private AlertService alertService;

//
//      Check alerts every 15 minutes
//     Uses cron expression: 0 */15 * * * * (every 15 minutes)
//     For testing, you can use: 0 */1 * * * * (every 1 minute)
//
    @Scheduled(cron = "0 */15 * * * *")
    public void checkAlertsScheduled() {
        logger.info("Starting scheduled alert check...");

        try {
            alertService.checkAndTriggerAlerts();
            logger.info("Scheduled alert check completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled alert check: {}", e.getMessage(), e);
        }
    }



    @Scheduled(cron = "0 1 * * * *")

    public void checkAlertsFrequent() {
        logger.info("Starting frequent alert check (testing)...");

        try {
            alertService.checkAndTriggerAlerts();
            logger.info("Frequent alert check completed successfully");
        } catch (Exception e) {
            logger.error("Error during frequent alert check: {}", e.getMessage(), e);
        }
    }
}
