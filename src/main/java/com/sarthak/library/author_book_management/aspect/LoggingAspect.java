package com.sarthak.library.author_book_management.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    // ─────────────────────────────────────────────────────────────────────────
    // CONTROLLER LAYER — logs HTTP method, URI, authenticated user, and args
    // ─────────────────────────────────────────────────────────────────────────

    @Before("execution(* com.sarthak.library.author_book_management.controller..*(..))")
    public void logControllerEntry(JoinPoint joinPoint) {
        String handler = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "." + joinPoint.getSignature().getName() + "()";

        // Resolve authenticated principal
        String principal = resolveAuthenticatedUser();

        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            log.info(">>> [CONTROLLER] {} {} | Handler: {} | User: {} | Args: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    handler,
                    principal,
                    Arrays.toString(joinPoint.getArgs()));
        } else {
            log.info(">>> [CONTROLLER] {} | User: {} | Args: {}",
                    handler,
                    principal,
                    Arrays.toString(joinPoint.getArgs()));
        }
    }

    @AfterReturning(
            pointcut = "execution(* com.sarthak.library.author_book_management.controller..*(..))",
            returning = "result"
    )
    public void logControllerExit(JoinPoint joinPoint, Object result) {
        String handler = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "." + joinPoint.getSignature().getName() + "()";
        log.info("<<< [CONTROLLER] {} | Response: {}", handler, result);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECURITY LAYER — logs authentication lookups via CustomUserDetailsService
    // Password is never logged; only username + granted roles are recorded.
    // ─────────────────────────────────────────────────────────────────────────

    @Around("execution(* com.sarthak.library.author_book_management.service.CustomUserDetailsService.loadUserByUsername(..))")
    public Object logUserDetailsLoad(ProceedingJoinPoint joinPoint) throws Throwable {
        String username = joinPoint.getArgs().length > 0
                ? String.valueOf(joinPoint.getArgs()[0])
                : "unknown";

        log.info(">>> [SECURITY] Authentication attempt for username: '{}'", username);
        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long elapsed = System.currentTimeMillis() - start;
        if (result instanceof UserDetails ud) {
            String roles = ud.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .collect(Collectors.joining(", "));
            log.info("<<< [SECURITY] User '{}' loaded | Enabled: {} | Roles: [{}] | Took: {} ms",
                    ud.getUsername(), ud.isEnabled(), roles, elapsed);
        }
        return result;
    }

    @AfterThrowing(
            pointcut = "execution(* com.sarthak.library.author_book_management.service.CustomUserDetailsService.*(..))",
            throwing = "ex"
    )
    public void logAuthFailure(JoinPoint joinPoint, Throwable ex) {
        String username = joinPoint.getArgs().length > 0
                ? String.valueOf(joinPoint.getArgs()[0])
                : "unknown";
        log.warn("!!! [SECURITY] Authentication FAILED for username: '{}' | Reason: {}",
                username, ex.getMessage());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SERVICE LAYER — logs method entry, exit, return value, and execution time
    // Excludes CustomUserDetailsService (handled separately above).
    // ─────────────────────────────────────────────────────────────────────────

    @Around("execution(* com.sarthak.library.author_book_management.service..*(..)) " +
            "&& !execution(* com.sarthak.library.author_book_management.service.CustomUserDetailsService.*(..))")
    public Object logServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "." + joinPoint.getSignature().getName() + "()";

        log.info(">>> [SERVICE] {} | Args: {}", method, Arrays.toString(joinPoint.getArgs()));
        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long elapsed = System.currentTimeMillis() - start;
        log.info("<<< [SERVICE] {} | Returned: {} | Took: {} ms", method, result, elapsed);
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EXCEPTION LOGGING — logs any exception thrown in controller or service
    // ─────────────────────────────────────────────────────────────────────────

    @AfterThrowing(
            pointcut = "execution(* com.sarthak.library.author_book_management.controller..*(..)) " +
                       "|| (execution(* com.sarthak.library.author_book_management.service..*(..)) " +
                       "&& !execution(* com.sarthak.library.author_book_management.service.CustomUserDetailsService.*(..)))",
            throwing = "ex"
    )
    public void logException(JoinPoint joinPoint, Throwable ex) {
        String method = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "." + joinPoint.getSignature().getName() + "()";
        log.error("!!! [EXCEPTION] in {} | {}: {}", method,
                ex.getClass().getSimpleName(), ex.getMessage());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPER — resolves the currently authenticated user from SecurityContext
    // ─────────────────────────────────────────────────────────────────────────

    private String resolveAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return "anonymous";
        }
        String username = auth.getName();
        String roles = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.joining(", "));
        return username + " [" + roles + "]";
    }
}
