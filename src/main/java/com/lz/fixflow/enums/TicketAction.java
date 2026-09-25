package com.lz.fixflow.enums;

import lombok.Getter;

@Getter
public enum TicketAction {

    CREATE("CREATE"),
    HANDLE("HANDLE"),
    ASSIGN("ASSIGN"),
    RESOLVE("RESOLVE"),
    REOPEN("REOPEN"),
    CLOSE("CLOSE"),
    CANCEL("CANCEL");

    private final String value;
    TicketAction(String value) {
        this.value = value;
    }
}
