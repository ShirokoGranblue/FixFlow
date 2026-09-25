package com.lz.fixflow.enums;

import lombok.Getter;

@Getter
public enum TicketStatus {

    OPEN("OPEN"),
    CLOSED("CLOSED"),
    IN_PROGRESS("IN_PROGRESS"),
    RESOLVED("RESOLVED"),
    CANCELLED("CANCELLED"),
    NULL("NULL");

    private final String value;

    TicketStatus(String value) {
        this.value = value;
    }
}
