package com.crm.audit.aspect;

import com.crm.audit.service.AuditLogService;
import com.crm.user.entity.User;
import com.crm.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;
    private final UserRepository userRepository;
    private final HttpServletRequest request;

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;

        if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            email = userDetails.getUsername();
        } else {
            email = principal.toString();
        }

        return userRepository.findByEmailAndActiveTrue(email).orElse(null);
    }

    // ✅ CREATE
    @AfterReturning(pointcut = "execution(* com.crm.*.service.*.create*(..))", returning = "result")
    public void logCreate(JoinPoint joinPoint, Object result) {
        User user = getCurrentUser();
        if (user == null || result == null) return;

        auditLogService.log(
                user,
                getModule(joinPoint),
                "CREATE",
                extractEntityId(result),
                null,
                safeToString(result)
        );
    }

    // ✅ UPDATE (null-check bilan)
    @AfterReturning(pointcut = "execution(* com.crm.*.service.*.update*(..))", returning = "result")
    public void logUpdate(JoinPoint joinPoint, Object result) {
        User user = getCurrentUser();
        if (user == null) return;

        Object[] args = joinPoint.getArgs();
        String oldVal = (args.length > 1 && args[1] != null) ? args[1].toString() : "N/A";
        String newVal = safeToString(result);

        auditLogService.log(
                user,
                getModule(joinPoint),
                "UPDATE",
                extractEntityId(result),
                oldVal,
                newVal
        );
    }

    // ✅ DELETE
    @AfterReturning("execution(* com.crm.*.service.*.delete*(..))")
    public void logDelete(JoinPoint joinPoint) {
        User user = getCurrentUser();
        if (user == null) return;

        String entityId = (joinPoint.getArgs().length > 0 && joinPoint.getArgs()[0] != null)
                ? joinPoint.getArgs()[0].toString()
                : "UNKNOWN";

        auditLogService.log(user, getModule(joinPoint), "DELETE", entityId, "EXISTED", "DELETED");
    }

    // ✅ LEAD CONTACT HISTORY
    @AfterReturning(pointcut = "execution(* com.crm.lead.service.LeadContactHistoryService.addHistory(..))", returning = "result")
    public void logAddLeadContactHistory(JoinPoint joinPoint, Object result) {
        User user = getCurrentUser();
        if (user == null || result == null) return;

        auditLogService.log(
                user,
                "LEAD_CONTACT_HISTORY",
                "CREATE",
                extractEntityId(result),
                null,
                safeToString(result)
        );
    }

    // 🔹 Module aniqlovchi yordamchi metod
    private String getModule(JoinPoint joinPoint) {
        String pkg = joinPoint.getTarget().getClass().getPackageName();
        if (pkg.contains("lead")) return "LEAD";
        if (pkg.contains("client")) return "CLIENT";
        if (pkg.contains("finance")) return "PAYMENT";
        if (pkg.contains("user")) return "USER";
        if (pkg.contains("file")) return "FILE";
        return "UNKNOWN";
    }

    // 🔹 Entity ID ni xavfsiz olish
    private String extractEntityId(Object result) {
        if (result == null) return "UNKNOWN";
        try {
            Object id = result.getClass().getMethod("getId").invoke(result);
            return id != null ? id.toString() : "UNKNOWN";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    // 🔹 Null-safe toString
    private String safeToString(Object obj) {
        return (obj == null) ? "null" : obj.toString();
    }
}
