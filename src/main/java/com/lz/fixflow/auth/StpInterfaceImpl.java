package com.lz.fixflow.auth;

import cn.dev33.satoken.stp.StpInterface;
import com.lz.fixflow.entity.User;
import com.lz.fixflow.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserMapper userMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType){
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType){

        String userid = loginId.toString();

        User user = userMapper.selectById(userid);

        if(user == null){
            return List.of();
        }

        return List.of(user.getRole());
    }
}
