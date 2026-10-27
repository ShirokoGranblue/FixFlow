package com.lz.fixflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lz.fixflow.annotation.Audit;
import com.lz.fixflow.common.BusinessException;
import com.lz.fixflow.common.ErrorCode;
import com.lz.fixflow.dto.TicketCommentDTO;
import com.lz.fixflow.entity.Ticket;
import com.lz.fixflow.entity.TicketComment;
import com.lz.fixflow.entity.User;
import com.lz.fixflow.enums.UserRole;
import com.lz.fixflow.mapper.TicketCommentMapper;
import com.lz.fixflow.mapper.TicketMapper;
import com.lz.fixflow.mapper.UserMapper;
import com.lz.fixflow.service.CommentService;
import com.lz.fixflow.service.PermissionService;
import com.lz.fixflow.vo.TicketCommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketCommentImpl implements CommentService {

    private final TicketCommentMapper ticketCommentMapper;
    private final TicketMapper ticketMapper;
    private final UserMapper userMapper;
    private final PermissionService permissionService;

    private TicketCommentVO convertToVO(TicketComment entity) {
        TicketCommentVO vo = new TicketCommentVO();

        if (entity == null) {
            throw new RuntimeException("内部错误");
        }

        vo.setId(entity.getId());
        vo.setContent(entity.getContent());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setAuthorId(entity.getAuthorId());
        vo.setTicketId(entity.getTicketId());

        return vo;
    }

    @Override
    public List<TicketCommentVO> getComment(String id){

        String userid = StpUtil.getLoginIdAsString();
        Ticket ticket = ticketMapper.selectById(id);

        permissionService.requireTicketAccess(userid,ticket);

        List<TicketComment> list = ticketCommentMapper.selectList(
                new LambdaQueryWrapper<TicketComment>()
                        .eq(TicketComment::getTicketId , id)
                        .orderByDesc(TicketComment::getCreatedAt)
        );

        return list.stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    @Audit("用户添加评论")
    public TicketCommentVO addComment(String id , TicketCommentDTO dto){

        String userid = StpUtil.getLoginIdAsString();

        TicketComment ticketComment = new TicketComment();
        Ticket ticket = ticketMapper.selectById(id);

        if (ticket == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "工单不存在");
        }

        permissionService.requireTicketAccess(userid,ticket);

        ticketComment.setId(UUID.randomUUID().toString());
        ticketComment.setAuthorId(userid);
        ticketComment.setTicketId(id);
        ticketComment.setContent(dto.getContent());
        ticketComment.setCreatedAt(LocalDateTime.now());

        ticketCommentMapper.insert(ticketComment);

        return convertToVO(ticketComment);
    }
}
