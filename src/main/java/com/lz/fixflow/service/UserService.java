package com.lz.fixflow.service;

import com.lz.fixflow.dto.UserLoginDTO;
import com.lz.fixflow.dto.UserQueryDTO;
import com.lz.fixflow.dto.UserRegisterDTO;
import com.lz.fixflow.dto.UserRoleUpdateDTO;
import com.lz.fixflow.vo.LoginVO;
import com.lz.fixflow.vo.UserVO;
import org.springframework.stereotype.Service;
import com.lz.fixflow.common.PageResult;
import java.util.List;

@Service
public interface UserService {

    UserVO register(UserRegisterDTO dto);

    LoginVO login(UserLoginDTO dto);

    Void logout();

    UserVO getCurrentUser();

    List<UserVO> getStaffUsers();

    PageResult<UserVO> getUsers(UserQueryDTO dto);

    UserVO updateRole(String id, UserRoleUpdateDTO dto);
}
