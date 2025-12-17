package com.mdd.auth.models;

import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Niveau de sévérité")
public enum Severity {
    ERROR,
    WARNING,
    INFO,
    SUCCESS;

    @JsonValue
    public String toJson() {
        return this.name().toLowerCase();
    }
}
