package com.mdd.auth.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Schema(description = "Enveloppe de réponse standard (compatible front)")
public class ApiResult<T> {

    private T data;
    private String message;
    private int status;
    private Instant timestamp;
    private String requestId;

    public ApiResult() {
        this.timestamp = Instant.now();
    }

    public ApiResult(T data, String message, int status, String requestId) {
        this.data = data;
        this.message = message;
        this.status = status;
        this.requestId = requestId;
        this.timestamp = Instant.now();
    }
}
