package com.wtcmessenger.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex, HttpServletRequest req) {
        return buildError(HttpStatus.FORBIDDEN, "FORBIDDEN", "Acesso negado. Você não tem permissão para realizar esta ação.", req.getRequestURI());
    }

    @ExceptionHandler(InvalidCampaignException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCampaign(InvalidCampaignException ex, HttpServletRequest req) {
        return buildError(HttpStatus.BAD_REQUEST, "INVALID_CAMPAIGN", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error", ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Ocorreu um erro interno. Por favor, tente novamente mais tarde.", req.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String code, String message, String path) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .code(code)
                .message(message)
                .path(path)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}