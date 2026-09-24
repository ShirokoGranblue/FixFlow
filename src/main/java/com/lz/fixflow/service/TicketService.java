package com.lz.fixflow.service;

import com.lz.fixflow.common.PageResult;
import com.lz.fixflow.dto.TicketAssignDTO;
import com.lz.fixflow.dto.TicketCreateDTO;
import com.lz.fixflow.dto.TicketQueryDTO;
import com.lz.fixflow.vo.TicketHistoryVO;
import com.lz.fixflow.vo.TicketVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TicketService {

    TicketVO createTicket(TicketCreateDTO dto);

    List<TicketVO> showTickets();

    TicketVO showTicket(String id);

    List<TicketVO> getOpenTickets();

    TicketVO handleTicket(String id);

    List<TicketVO> getAssignedTickets();

    TicketVO resolveTicket(String id);

    TicketVO closeTicket(String id);

    List<TicketHistoryVO> getTicketHistory(String id);

    PageResult<TicketVO> searchTickets(TicketQueryDTO dto);

    TicketVO assignTicket(String id ,TicketAssignDTO dto);
}
