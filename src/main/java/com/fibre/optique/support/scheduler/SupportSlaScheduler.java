package com.fibre.optique.support.scheduler;

import com.fibre.optique.support.service.SupportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled SLA enforcement for the support module.
 * Requires {@code @EnableScheduling} on the main application class.
 */
@Component
public class SupportSlaScheduler {

    private static final Logger log = LoggerFactory.getLogger(SupportSlaScheduler.class);

    private final SupportService supportService;

    public SupportSlaScheduler(SupportService supportService) {
        this.supportService = supportService;
    }

    /**
     * Checks for SLA breaches every hour.
     * Escalates breached tickets to CRITIQUE and notifies the admin on call.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void checkSlaBreaches() {
        log.info("SupportSlaScheduler: running SLA breach check");
        try {
            supportService.checkSlaBreaches();
        } catch (Exception e) {
            log.error("SupportSlaScheduler: SLA check failed", e);
        }
    }
}
