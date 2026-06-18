package com.fibre.optique.common.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * AOP Aspect — Service Layer Logging
 *
 * <p>Intercepts every method in every {@code @Service} class and logs:</p>
 * <ul>
 *   <li>Entry: method name + arguments (DEBUG)</li>
 *   <li>Exit: return value summary (DEBUG)</li>
 *   <li>Exception: error message + type (ERROR)</li>
 * </ul>
 *
 * <p>Pointcut targets all classes in any {@code .service} package of the project.</p>
 */
@Aspect
@Component
public class LoggingAspect {

    // One logger per calling class
    private Logger logger(JoinPoint jp) {
        return LoggerFactory.getLogger(jp.getSignature().getDeclaringTypeName());
    }

    /** Pointcut: any method in any service package */
    @Pointcut("within(com.fibre.optique..service..*)")
    void serviceLayer() {}

    /** Pointcut: any method in any controller package */
    @Pointcut("within(com.fibre.optique..controller..*)")
    void controllerLayer() {}

    // -------------------------------------------------------------------------
    // Service layer — entry / exit / exception
    // -------------------------------------------------------------------------

    @Before("serviceLayer()")
    void logServiceEntry(JoinPoint jp) {
        Logger log = logger(jp);
        if (log.isDebugEnabled()) {
            log.debug("→ {}.{}({})",
                    jp.getSignature().getDeclaringType().getSimpleName(),
                    jp.getSignature().getName(),
                    Arrays.toString(jp.getArgs()));
        }
    }

    @AfterReturning(pointcut = "serviceLayer()", returning = "result")
    void logServiceExit(JoinPoint jp, Object result) {
        Logger log = logger(jp);
        if (log.isDebugEnabled()) {
            log.debug("← {}.{} returned {}",
                    jp.getSignature().getDeclaringType().getSimpleName(),
                    jp.getSignature().getName(),
                    result != null ? result.getClass().getSimpleName() : "null");
        }
    }

    @AfterThrowing(pointcut = "serviceLayer()", throwing = "ex")
    void logServiceException(JoinPoint jp, Throwable ex) {
        Logger log = logger(jp);
        log.error("✗ {}.{} threw {}: {}",
                jp.getSignature().getDeclaringType().getSimpleName(),
                jp.getSignature().getName(),
                ex.getClass().getSimpleName(),
                ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // Controller layer — INFO level request logging
    // -------------------------------------------------------------------------

    @Before("controllerLayer()")
    void logControllerEntry(JoinPoint jp) {
        Logger log = logger(jp);
        if (log.isInfoEnabled()) {
            log.info("▶ REST {}.{}",
                    jp.getSignature().getDeclaringType().getSimpleName(),
                    jp.getSignature().getName());
        }
    }
}
