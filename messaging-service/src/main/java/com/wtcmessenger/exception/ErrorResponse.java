package com.wtcmessenger.exception;

import lombok.Builder;
import lombok.Getter;
import java.time.Instant;
import java.util.Map;

@Getter
@Builder
public class ErrorResponse {
    private Instant timestamp;
    private int status;
    private String code;
    private String message;
    private String path;
    private Map<String, String> fieldErrors;
}
