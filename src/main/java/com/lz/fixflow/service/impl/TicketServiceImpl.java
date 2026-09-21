package com.lz.fixflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lz.fixflow.common.BusinessException;
import com.lz.fixflow.dto.TicketCreateDTO;
import com.lz.fixflow.entity.Ticket;
import com.lz.fixflow.entity.User;
import com.lz.fixflow.mapper.TicketMapper;
import com.lz.fixflow.mapper.UserMapper;
import com.lz.fixflow.service.TicketService;
import com.lz.fixflow.vo.TicketVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketMapper ticketMapper;
    private final UserMapper userMapper;

    private TicketVO convertToVO(Ticket ticket) {
        TicketVO vo = new TicketVO();

        vo.setId(UUID.randomUUID().toString());
        vo.setTitle(ticket.getTitle());
        vo.setDescription(ticket.getDescription());
        vo.setPriority(ticket.getPriority());
        vo.setStatus(ticket.getStatus());
        vo.setCreatorId(ticket.getCreatorId());
        vo.setCreatedAt(ticket.getCreatedAt());
        vo.setAssigneeId(ticket.getAssigneeId());

        return vo;
    }

    private void checkStaffPermission(String id){

        User user = userMapper.selectById(id);

        if(user.getId()==null){
            throw new BusinessException("工单或用户不存在");
        }

        if(!Objects.equals(user.getRole(), "STAFF")
                &&!Objects.equals(user.getRole(), "ADMIN")){
            throw new BusinessException("无权处理");
        }
    }

    @Override
    public TicketVO createTicket(TicketCreateDTO dto){
        String userid = StpUtil.getLoginIdAsString();
        Ticket ticket = new Ticket();

        ticket.setId(UUID.randomUUID().toString());
        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setPriority(dto.getPriority());
        ticket.setStatus("OPEN");
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        ticket.setCreatorId(userid);

        ticketMapper.insert(ticket);
        return convertToVO(ticket);
    }

    @Override
    public List<TicketVO> showTickets() {
        String id = StpUtil.getLoginIdAsString();
        List<Ticket> list = ticketMapper.selectList(new LambdaQueryWrapper<Ticket>()
                .eq(Ticket::getCreatorId,id)
                .orderByDesc(Ticket::getCreatedAt)
        );
        return list.stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public TicketVO showTicket(String id) {
        Ticket ticket = ticketMapper.selectById(id);
        String userid = StpUtil.getLoginIdAsString();

        if(ticket == null){
            throw new BusinessException("工单不存在");
        }

        if(!ticket.getCreatorId().equals(userid)) {
            throw new BusinessException("无权查看");
        }

        return convertToVO(ticket);
    }

    @Override
    public List<TicketVO> getOpenTickets() {
        String userid = StpUtil.getLoginIdAsString();
        checkStaffPermission(userid);
        List<Ticket> list = ticketMapper.selectList(new LambdaQueryWrapper<Ticket>()
                .eq(Ticket::getCreatorId,userid)
                .eq(Ticket::getStatus,"OPEN")
                .isNull(Ticket::getAssigneeId)
                .orderByAsc(Ticket::getCreatedAt)
        );

        return list.stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public TicketVO handleTickets(String id) {
        String userid = StpUtil.getLoginIdAsString();
        checkStaffPermission(userid);

        int row = ticketMapper.update(null,
                new LambdaUpdateWrapper<Ticket>()
                        .eq(Ticket::getStatus,"OPEN")
                        .eq(Ticket::getId,id)
                        .isNull(Ticket::getAssigneeId)
                        .set(Ticket::getStatus,"IN_PROGRESS")
                        .set(Ticket::getAssigneeId,userid)
                );

        if(row==0){
            throw new BusinessException("工单不存在或已经被处理");
        }

        Ticket ticket = ticketMapper.selectById(id);

        return convertToVO(ticket);
    }
}
