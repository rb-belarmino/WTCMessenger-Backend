package br.com.fiap.crm_backend.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String resource, String id) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", resource + " não encontrado: " + id);
    }
}