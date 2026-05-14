package br.com.fiap.crm_backend.exception;

import org.springframework.http.HttpStatus;

public class TokenExpiredException extends ApiException {

    public TokenExpiredException() {
        super("Token expirado",
                HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED");
    }
}