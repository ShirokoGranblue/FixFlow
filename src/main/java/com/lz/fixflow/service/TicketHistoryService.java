package com.lz.fixflow.service;

import org.springframework.stereotype.Service;

@Service
public interface TicketHistoryService {

    void recordHistory(String ticketid,
            String operatorid,
            String action,
            String fromStatus,
            String toStatus);
}
