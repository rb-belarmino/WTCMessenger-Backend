
package br.com.fiap.crm_backend.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException() {
        super("Credenciais inválidas",
                HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
    }
}