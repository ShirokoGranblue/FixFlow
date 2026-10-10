package com.lz.fixflow.controller;

import cn.dev33.satoken.annotation.SaCheckRole;

import com.lz.fixflow.common.PageResult;
import com.lz.fixflow.common.Result;
import com.lz.fixflow.dto.UserQueryDTO;
import com.lz.fixflow.dto.UserRoleUpdateDTO;
import com.lz.fixflow.service.UserService;
import com.lz.fixflow.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@SaCheckRole(value = "ADMIN")
public class AdminController {

    private final UserService userService;

    @GetMapping("/staff")
    public Result<List<UserVO>> getStaffUsers(){
        return Result.success(userService.getStaffUsers());
    }

    @GetMapping("/users")
    public Result<PageResult<UserVO>> getUsers(@Valid UserQueryDTO dto){
        return Result.success(userService.getUsers(dto));
    }
    
    @PostMapping("/{id}/role")
    public Result<UserVO> updateUser(@PathVariable String id ,
                                     @Valid @RequestBody UserRoleUpdateDTO dto){
        return Result.success(userService.updateRole(id ,dto));
    }
}
