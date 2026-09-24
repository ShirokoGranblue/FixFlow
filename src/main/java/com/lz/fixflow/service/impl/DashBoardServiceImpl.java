package com.lz.fixflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lz.fixflow.common.BusinessException;
import com.lz.fixflow.entity.Ticket;
import com.lz.fixflow.entity.User;
import com.lz.fixflow.mapper.CategoryMapper;
import com.lz.fixflow.mapper.TicketMapper;
import com.lz.fixflow.mapper.UserMapper;
import com.lz.fixflow.service.DashBoardService;
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

    @Override
    public DashBoardVO getDashBoard() {

        String userid = StpUtil.getLoginIdAsString();

        checkPermission(userid);

        DashBoardVO vo = new DashBoardVO();


        vo.setTotalCount(ticketMapper.selectCount(null));

        vo.setOpenCount(ticketMapper.selectCount(
                new LambdaQueryWrapper<Ticket>()
                        .eq(Ticket::getStatus,"OPEN")
        ));

        vo.setInProgressCount(ticketMapper.selectCount(
                new LambdaQueryWrapper<Ticket>()
                        .eq(Ticket::getStatus,"IN_PROGRESS")
        ));

        vo.setResolvedCount(ticketMapper.selectCount(
                new LambdaQueryWrapper<Ticket>()
                        .eq(Ticket::getStatus,"RESOLVED")
        ));

        vo.setClosedCount(ticketMapper.selectCount(
                new LambdaQueryWrapper<Ticket>()
                    .eq(Ticket::getStatus,"CLOSED")
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

    private void checkPermission(String userid){

        User user = userMapper.selectById(userid);

        if(userid==null){
            throw new BusinessException("用户不存在或未登录");
        }

        if(!(Objects.equals(user.getRole(), "STAFF"))
                && !(Objects.equals(user.getRole(), "ADMIN"))){

            throw new BusinessException("无权查看Dashboard");
        }
    }
}
