package ru.didcvee.ecd.security.advice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class SecurityExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("status", 403, "message", "Доступ запрещён", "path", req.getRequestURI()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuth(AuthenticationException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("status", 401, "message", "Ошибка аутентификации", "path", req.getRequestURI()));
    }
}
