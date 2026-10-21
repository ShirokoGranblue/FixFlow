package com.lz.fixflow.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.lz.fixflow.entity.TicketHistory;
import com.lz.fixflow.mapper.TicketHistoryMapper;
import com.lz.fixflow.service.TicketHistoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketHistoryServiceImpl implements TicketHistoryService{

    private final TicketHistoryMapper ticketHistoryMapper;

    @Override
    @Transactional(propagation=Propagation.REQUIRED , rollbackFor = Exception.class)
    public void recordHistory(
            String ticketid,
            String operatorid,
            String action,
            String fromStatus,
            String toStatus) {

        TicketHistory ticketHistory = new TicketHistory();
            ticketHistory.setId(UUID.randomUUID().toString());
            ticketHistory.setTicketId(ticketid);
            ticketHistory.setOperatorId(operatorid);
            ticketHistory.setAction(action);
            ticketHistory.setFromStatus(fromStatus);
            ticketHistory.setToStatus(toStatus);
            ticketHistory.setCreatedAt(LocalDateTime.now());

            ticketHistoryMapper.insert(ticketHistory);
    }
}
