package br.com.fiap.crm_backend.exception;

import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends ApiException {

    public DuplicateEmailException(String email) {
        super("Email já cadastrado: " + email,
                HttpStatus.CONFLICT, "DUPLICATE_EMAIL");
    }
}