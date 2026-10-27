package com.lz.fixflow.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import com.lz.fixflow.common.BusinessException;
import org.springframework.stereotype.Service;
import com.lz.fixflow.common.ErrorCode;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lz.fixflow.dto.CursorPageResponseDTO;
import com.lz.fixflow.dto.TicketCursor;
import com.lz.fixflow.entity.Ticket;
import com.lz.fixflow.enums.TicketStatus;
import com.lz.fixflow.mapper.TicketMapper;
import com.lz.fixflow.service.TicketCursorService;
import com.lz.fixflow.vo.TicketListItemVO;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class TicketCursorServiceImpl implements TicketCursorService{

    private final TicketMapper ticketMapper;

    @Override 
    public CursorPageResponseDTO<TicketListItemVO> queryOpenTicketByCursor(
                                                                    int size,
                                                                    LocalDateTime cursorTime,
                                                                    String cursorId){
        if (size<1||size>100) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT,"size 必须在1-100之间");
        }
        if ((cursorTime!=null) != (cursorId!=null)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT,"游标参数不完整");
        }

        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<Ticket>()
                                                .eq(Ticket::getStatus,TicketStatus.OPEN.getValue());

        if (cursorTime!=null) {
            wrapper.and(w-> w.lt(Ticket::getCreatedAt, cursorTime)
                                .or(y-> y.eq(Ticket::getCreatedAt, cursorTime)
                                    .lt(Ticket::getId, cursorId))                        
                                );
        }

        wrapper.orderByDesc(Ticket::getCreatedAt, Ticket::getId);

        Page<Ticket> page = new Page<>(1,size+1L,false);

        List<Ticket> queried = ticketMapper.selectPage(page, wrapper).getRecords();

        boolean hasmore = queried.size() > size;

        List<Ticket> current = hasmore ? queried.subList(0, size) : queried;

        List<TicketListItemVO> result = current.stream()
                                            .map(x-> {
                                                TicketListItemVO vo = new TicketListItemVO();
                                                    vo.setId(x.getId());
                                                    vo.setCreated_at(x.getCreatedAt());
                                                    vo.setStatus(x.getStatus());
                                                    vo.setTitle(x.getTitle());
                                                    return vo;
                                            })
                                            .toList();

        TicketCursor nextCursor = null;
        if (hasmore) {
            Ticket last = current.get(current.size()-1);

            nextCursor = new TicketCursor();
            nextCursor.setCreated_at(last.getCreatedAt());
            nextCursor.setId(last.getId());
        }

        return new CursorPageResponseDTO<TicketListItemVO>(result,hasmore,nextCursor);
    }                         
}
