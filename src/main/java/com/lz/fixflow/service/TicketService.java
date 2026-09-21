package com.lz.fixflow.service;

import com.lz.fixflow.dto.TicketCreateDTO;
import com.lz.fixflow.vo.TicketVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TicketService {

    TicketVO createTicket(TicketCreateDTO dto);

    List<TicketVO> showTickets();

    TicketVO showTicket(String id);

    List<TicketVO> getOpenTickets();

    TicketVO handleTickets(String id);
}
