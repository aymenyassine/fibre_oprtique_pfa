package com.fibre.optique.billing.scheduler;

import com.fibre.optique.billing.service.BillingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled billing tasks.
 *
 * <p>Requires {@code @EnableScheduling} on the main application class.</p>
 */
@Component
public class BillingScheduler {

    private static final Logger log = LoggerFactory.getLogger(BillingScheduler.class);

    private final BillingService billingService;

    public BillingScheduler(BillingService billingService) {
        this.billingService = billingService;
    }

    /**
     * Generates monthly invoices for all active subscriptions.
     * Runs on the 1st of every month at 01:00 AM.
     */
    @Scheduled(cron = "0 0 1 1 * *")
    public void generateMonthlyInvoices() {
        log.info("BillingScheduler: starting monthly invoice generation");
        try {
            billingService.generateMonthlyInvoices();
            log.info("BillingScheduler: monthly invoice generation completed");
        } catch (Exception e) {
            log.error("BillingScheduler: monthly invoice generation failed", e);
        }
    }

    /**
     * Marks overdue invoices as EN_RETARD and sends payment reminders.
     * Runs every day at 03:00 AM.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void checkLatePayments() {
        log.info("BillingScheduler: checking late payments");
        try {
            billingService.markOverdueInvoices();
            log.info("BillingScheduler: late payment check completed");
        } catch (Exception e) {
            log.error("BillingScheduler: late payment check failed", e);
        }
    }
}
