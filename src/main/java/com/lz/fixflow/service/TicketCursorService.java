package com.lz.fixflow.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.lz.fixflow.dto.CursorPageResponseDTO;
import com.lz.fixflow.vo.TicketListItemVO;

@Service 
public interface TicketCursorService {
    CursorPageResponseDTO<TicketListItemVO> queryOpenTicketByCursor(int size,LocalDateTime cursorTime,String cursorId);
}
