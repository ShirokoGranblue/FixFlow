package com.lz.fixflow.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor 
@NoArgsConstructor 
public class CursorPageResponseDTO<T> {
    private List<T> record;
    private boolean hasmore;
    private TicketCursor TicketCursor;
}
