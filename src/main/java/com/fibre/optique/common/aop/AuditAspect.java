package com.fibre.optique.common.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * AOP Aspect — Write-Operation Audit Trail
 *
 * <p>After any state-mutating service method succeeds, logs an AUDIT entry with:
 * the authenticated user, the operation name, and the return value type.
 * This provides an immutable audit trail in the application logs.</p>
 *
 * <p>Pointcut targets all {@code create*}, {@code update*}, {@code delete*},
 * {@code suspend*}, {@code terminate*}, {@code cancel*}, {@code reject*},
 * {@code complete*}, {@code resolve*}, {@code close*} methods in service classes.</p>
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    @Pointcut("within(com.fibre.optique..service..*) && (" +
              "execution(* create*(..)) || " +
              "execution(* update*(..)) || " +
              "execution(* delete*(..)) || " +
              "execution(* suspend*(..)) || " +
              "execution(* terminate*(..)) || " +
              "execution(* cancel*(..)) || " +
              "execution(* reject*(..))  || " +
              "execution(* complete*(..)) || " +
              "execution(* resolve*(..)) || " +
              "execution(* close*(..))   || " +
              "execution(* deactivate*(..)) || " +
              "execution(* reactivate*(..)) || " +
              "execution(* schedule*(..)) || " +
              "execution(* recordPayment(..)) || " +
              "execution(* assignAgent(..))" +
              ")")
    void writeOperations() {}

    @AfterReturning(pointcut = "writeOperations()", returning = "result")
    void auditWriteOperation(JoinPoint jp, Object result) {
        String actor = resolveCurrentUser();
        audit.info("[AUDIT] user={} | operation={}.{} | result={}",
                actor,
                jp.getSignature().getDeclaringType().getSimpleName(),
                jp.getSignature().getName(),
                result != null ? result.getClass().getSimpleName() + "#" + extractId(result) : "void"
        );
    }

    // -------------------------------------------------------------------------

    private String resolveCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                return auth.getName();
            }
        } catch (Exception ignored) {
            // May be null in async/scheduler context
        }
        return "SYSTEM";
    }

    /**
     * Attempts to extract an id field from the result for logging.
     * Falls back gracefully if the result has no accessible id.
     */
    private String extractId(Object result) {
        try {
            var idMethod = result.getClass().getMethod("getId");
            Object id = idMethod.invoke(result);
            return id != null ? id.toString() : "?";
        } catch (Exception e) {
            return "?";
        }
    }
}
