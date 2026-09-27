package com.lz.fixflow.enums;

import lombok.Getter;

@Getter
public enum TicketPriority {

    LOW("LOW"),
    MEDIUM("MEDIUM"),
    HIGH("HIGH"),
    URGENT("URGENT");

    private final String value;
    TicketPriority(String value) {
        this.value = value;
    }
}
