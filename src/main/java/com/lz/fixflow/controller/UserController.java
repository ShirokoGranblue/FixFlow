package com.lz.fixflow.controller;

import com.lz.fixflow.common.Result;
import com.lz.fixflow.dto.UserLoginDTO;
import com.lz.fixflow.dto.UserRegisterDTO;
import com.lz.fixflow.service.UserService;
import com.lz.fixflow.vo.LoginVO;
import com.lz.fixflow.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody UserRegisterDTO dto) {
        return Result.success(userService.register(dto));
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody UserLoginDTO dto){
        return Result.success(userService.login(dto));
    }

    @GetMapping("/me")
    public Result<UserVO> me(){
        return Result.success(userService.getCurrentUser());
    }

    @PostMapping("/logout")
    public Result<Void> logout(){
        return Result.success(userService.logout());
    }
}
