package com.lz.fixflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lz.fixflow.common.BusinessException;
import com.lz.fixflow.dto.UserLoginDTO;
import com.lz.fixflow.dto.UserRegisterDTO;
import com.lz.fixflow.entity.User;
import com.lz.fixflow.mapper.UserMapper;
import com.lz.fixflow.service.UserService;
import com.lz.fixflow.vo.LoginVO;
import com.lz.fixflow.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private UserVO convertToVO(User user) {

        UserVO vo = new UserVO();
        vo.setId(UUID.randomUUID().toString());
        vo.setUsername(user.getUsername());
        vo.setRole(user.getRole());
        vo.setEmail(user.getEmail());
        vo.setCreatedAt(user.getCreatedAt());

        return vo;
    }

    @Override
    public UserVO register(UserRegisterDTO dto){
        User user = new User();

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>();

        long count = userMapper.selectCount(
                wrapper.eq(User::getUsername, dto.getUsername())
        );

        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        user.setId(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());

        userMapper.insert(user);

        return convertToVO(user);
    }

    @Override
    public LoginVO login(UserLoginDTO dto)
    {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>();

        User user = userMapper.selectOne(
                wrapper.eq(User::getUsername , dto.getUsername())
        );

        if(user==null){
            throw new BusinessException("用户不存在");
        }

        if(!passwordEncoder.matches(dto.getPassword(),user.getPassword())){
            throw new BusinessException("用户名或密码错误");
        }

        StpUtil.login(user.getId());

        LoginVO vo = new LoginVO();
        vo.setUser(convertToVO(user));
        vo.setSatokenName(StpUtil.getTokenName());
        vo.setSatokenValue(StpUtil.getTokenValue());

        return vo;
    }
}
