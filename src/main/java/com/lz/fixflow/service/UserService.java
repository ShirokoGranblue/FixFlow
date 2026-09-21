package com.lz.fixflow.service;

import com.lz.fixflow.dto.UserLoginDTO;
import com.lz.fixflow.dto.UserRegisterDTO;
import com.lz.fixflow.vo.LoginVO;
import com.lz.fixflow.vo.UserVO;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    UserVO register(UserRegisterDTO dto);

    LoginVO login(UserLoginDTO dto);

}
