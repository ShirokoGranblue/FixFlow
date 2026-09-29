package com.lz.fixflow.service;

import com.lz.fixflow.dto.TicketCommentDTO;
import com.lz.fixflow.vo.TicketCommentVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CommentService {

    List<TicketCommentVO> getComment(String id);

    TicketCommentVO addComment(String id , TicketCommentDTO dto);
}
