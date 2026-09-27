package com.lz.fixflow.service;

import com.lz.fixflow.dto.UserLoginDTO;
import com.lz.fixflow.dto.UserRegisterDTO;
import com.lz.fixflow.dto.UserRoleUpdateDTO;
import com.lz.fixflow.vo.LoginVO;
import com.lz.fixflow.vo.UserVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    UserVO register(UserRegisterDTO dto);

    LoginVO login(UserLoginDTO dto);

    UserVO getCurrentUser();

    List<UserVO> getStaffUsers();

    UserVO updateRole(String id, UserRoleUpdateDTO dto);
}
