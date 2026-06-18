package com.fibre.optique.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AOP Aspect — Performance Monitoring
 *
 * <p>Wraps every service method and logs a WARN if execution exceeds
 * {@value #SLOW_THRESHOLD_MS} ms. Helps identify slow DB queries or
 * heavy operations at runtime without adding timer code to every method.</p>
 */
@Aspect
@Component
public class PerformanceAspect {

    private static final Logger log = LoggerFactory.getLogger(PerformanceAspect.class);

    /** Threshold in milliseconds above which a method execution is considered slow. */
    private static final long SLOW_THRESHOLD_MS = 500L;

    @Pointcut("within(com.fibre.optique..service..*)")
    void serviceLayer() {}

    @Around("serviceLayer()")
    Object measureExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return pjp.proceed();
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            if (elapsed > SLOW_THRESHOLD_MS) {
                log.warn("⚠ SLOW METHOD [{} ms] — {}.{}",
                        elapsed,
                        pjp.getSignature().getDeclaringType().getSimpleName(),
                        pjp.getSignature().getName());
            } else if (log.isTraceEnabled()) {
                log.trace("⏱ [{} ms] {}.{}",
                        elapsed,
                        pjp.getSignature().getDeclaringType().getSimpleName(),
                        pjp.getSignature().getName());
            }
        }
    }
}
