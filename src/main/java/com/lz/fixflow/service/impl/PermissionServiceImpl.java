package com.lz.fixflow.service.impl;

import com.lz.fixflow.common.BusinessException;
import com.lz.fixflow.entity.Ticket;
import com.lz.fixflow.entity.User;
import com.lz.fixflow.enums.UserRole;
import com.lz.fixflow.mapper.UserMapper;
import com.lz.fixflow.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final UserMapper userMapper;

    @Override
    public void requireAdmin(String userid){

        User user = userMapper.selectById(userid);

        if(user==null){
            throw new BusinessException("用户不存在");
        }

        if(!UserRole.ADMIN.getValue().equals(user.getRole())){
            throw new BusinessException("权限不足，无权访问");
        }
    }

    @Override
    public void requireStaff(String userid){

        User user = userMapper.selectById(userid);

        if(user==null){
            throw new BusinessException("用户不存在");
        }

        boolean staff = UserRole.STAFF.getValue().equals(user.getRole());
        boolean admin = UserRole.ADMIN.getValue().equals(user.getRole());

        if(!staff && !admin){
            throw new BusinessException("无权处理工单");
        }
    }

    @Override
    public void requireTicketAccess(String userid , Ticket ticket){

        if(ticket==null){
            throw new BusinessException("工单不存在");
        }

        if(userid.equals(ticket.getCreatorId())){
            return;
        }

        if(userid.equals(ticket.getAssigneeId())){
            return;
        }

        User user = userMapper.selectById(userid);

        if(user==null){
            throw new BusinessException("用户不存在");
        }

        if(UserRole.ADMIN.getValue().equals(user.getRole())){
            return;
        }

        throw new BusinessException("无权访问工单");
    }
}
