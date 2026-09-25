package com.lz.fixflow.vo;

import lombok.Data;

import java.util.List;

@Data
public class TicketDetailVO {

    private TicketVO ticket;
    private List<TicketHistoryVO> ticketHistory;
    private List<TicketCommentVO> ticketComments;
}
