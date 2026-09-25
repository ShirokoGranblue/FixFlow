package com.lz.fixflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lz.fixflow.common.BusinessException;
import com.lz.fixflow.entity.Ticket;
import com.lz.fixflow.entity.User;
import com.lz.fixflow.enums.TicketStatus;
import com.lz.fixflow.enums.UserRole;
import com.lz.fixflow.mapper.CategoryMapper;
import com.lz.fixflow.mapper.TicketMapper;
import com.lz.fixflow.mapper.UserMapper;
import com.lz.fixflow.service.DashBoardService;
import com.lz.fixflow.service.PermissionService;
import com.lz.fixflow.vo.DashBoardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DashBoardServiceImpl implements DashBoardService {

    private final TicketMapper ticketMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final PermissionService permissionService;

    @Override
    public DashBoardVO getDashBoard() {

        String userid = StpUtil.getLoginIdAsString();

        permissionService.requireStaff(userid);

        DashBoardVO vo = new DashBoardVO();

        vo.setTotalCount(ticketMapper.selectCount(null));

        vo.setOpenCount(ticketMapper.selectCount(
                new LambdaQueryWrapper<Ticket>()
                        .eq(Ticket::getStatus,TicketStatus.OPEN.getValue())
        ));

        vo.setInProgressCount(ticketMapper.selectCount(
                new LambdaQueryWrapper<Ticket>()
                        .eq(Ticket::getStatus,TicketStatus.IN_PROGRESS.getValue())
        ));

        vo.setResolvedCount(ticketMapper.selectCount(
                new LambdaQueryWrapper<Ticket>()
                        .eq(Ticket::getStatus,TicketStatus.RESOLVED.getValue())
        ));

        vo.setClosedCount(ticketMapper.selectCount(
                new LambdaQueryWrapper<Ticket>()
                    .eq(Ticket::getStatus, TicketStatus.CLOSED.getValue())
        ));

        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        vo.setTodayCount(ticketMapper.selectCount(
                new LambdaQueryWrapper<Ticket>()
                        .ge(Ticket::getCreatedAt,start)
                        .lt(Ticket::getCreatedAt,end)
        ));

        vo.setCategoryCount(categoryMapper.countCategory());

        return vo;
    }

}
