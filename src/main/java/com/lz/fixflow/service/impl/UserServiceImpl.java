package com.lz.fixflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lz.fixflow.common.BusinessException;
import com.lz.fixflow.dto.UserLoginDTO;
import com.lz.fixflow.dto.UserRegisterDTO;
import com.lz.fixflow.dto.UserRoleUpdateDTO;
import com.lz.fixflow.entity.User;
import com.lz.fixflow.mapper.UserMapper;
import com.lz.fixflow.service.UserService;
import com.lz.fixflow.vo.LoginVO;
import com.lz.fixflow.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private UserVO convertToVO(User entity) {
        UserVO vo = new UserVO();

        if (entity == null) {
            throw new RuntimeException("");
        }

        vo.setId(UUID.randomUUID().toString());
        vo.setUsername(entity.getUsername());
        vo.setRole(entity.getRole());
        vo.setEmail(entity.getEmail());
        vo.setCreatedAt(entity.getCreatedAt());

        return vo;
    }
    private void checkAdmin(String userid) {

        User user = userMapper.selectById(userid);

        if (userid == null) {
            throw new BusinessException("用户不存在");
        }
        if(!user.getRole().equals("ADMIN")){
            throw new BusinessException("你无权查看");
        }
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

    @Override
    public List<UserVO> getStaffUsers(){

        String userid = StpUtil.getLoginIdAsString();
        checkAdmin(userid);

        List<User> list = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .eq(User::getRole, "STAFF")
        );

        return list.stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public UserVO updateRole(String id, UserRoleUpdateDTO dto){

        String adminid = StpUtil.getLoginIdAsString();
        checkAdmin(adminid);

        if(id.equals(adminid)){
            throw new BusinessException("不能修改自己的角色");
        }

        int row = userMapper.update(null,
                new LambdaUpdateWrapper<User>()
                        .eq(User::getId, id)
                        .ne(User::getRole,"ADMIN")
                        .ne(User::getRole,dto.getRole())
                        .set(User::getRole,dto.getRole())
        );

        if (row==0){
            throw new BusinessException("用户不存在或无法修改");
        }

        User user = userMapper.selectById(id);

        return convertToVO(user);
    }
}
