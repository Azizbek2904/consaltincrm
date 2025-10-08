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
            // Agar Spring Security UserDetails bo‘lsa
            email = userDetails.getUsername(); // bu yerda email saqlanadi
        } else {
            // Oddiy string bo‘lsa
            email = principal.toString();
        }

        return userRepository.findByEmailAndActiveTrue(email).orElse(null);
    }


    // CREATE
    @AfterReturning(pointcut = "execution(* com.crm.*.service.*.create*(..))", returning = "result")
    public void logCreate(JoinPoint joinPoint, Object result) {
        User user = getCurrentUser();
        if (user != null) {
            auditLogService.log(user, getModule(joinPoint), "CREATE",
                    extractEntityId(result), null, result.toString());
        }
    }

    // UPDATE
    @AfterReturning(pointcut = "execution(* com.crm.*.service.*.update*(..))", returning = "result")
    public void logUpdate(JoinPoint joinPoint, Object result) {
        User user = getCurrentUser();
        if (user != null) {
            Object oldVal = joinPoint.getArgs()[1]; // eski request
            auditLogService.log(user, getModule(joinPoint), "UPDATE",
                    extractEntityId(result), oldVal.toString(), result.toString());
        }
    }

    // DELETE
    @AfterReturning("execution(* com.crm.*.service.*.delete*(..))")
    public void logDelete(JoinPoint joinPoint) {
        User user = getCurrentUser();
        if (user != null) {
            String entityId = joinPoint.getArgs()[0].toString();
            auditLogService.log(user, getModule(joinPoint), "DELETE", entityId, "EXISTED", "DELETED");
        }
    }

    private String getModule(JoinPoint joinPoint) {
        String pkg = joinPoint.getTarget().getClass().getPackageName();
        if (pkg.contains("lead")) return "LEAD";
        if (pkg.contains("client")) return "CLIENT";
        if (pkg.contains("finance")) return "PAYMENT";
        if (pkg.contains("user")) return "USER";
        if (pkg.contains("file")) return "FILE";
        return "UNKNOWN";
    }

    private String extractEntityId(Object result) {
        try {
            return result.getClass().getMethod("getId").invoke(result).toString();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
    @AfterReturning(pointcut = "execution(* com.crm.lead.service.LeadContactHistoryService.addHistory(..))", returning = "result")
    public void logAddLeadContactHistory(JoinPoint joinPoint, Object result) {
        User user = getCurrentUser();
        if (user != null) {
            auditLogService.log(user, "LEAD_CONTACT_HISTORY", "CREATE",
                    extractEntityId(result), null, result.toString());
        }

    }}
