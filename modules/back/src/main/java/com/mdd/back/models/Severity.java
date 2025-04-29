package com.mdd.back.models;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Severity {
    ERROR,
    WARNING,
    INFO,
    SUCCESS;

    @JsonValue
    public String toJson() {
        return this.name().toLowerCase(); // Sérialisation en minuscule
    }
}

