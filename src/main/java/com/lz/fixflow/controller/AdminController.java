package com.lz.fixflow.controller;

import com.lz.fixflow.common.Result;
import com.lz.fixflow.dto.UserRoleUpdateDTO;
import com.lz.fixflow.service.UserService;
import com.lz.fixflow.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/user")
public class AdminController {

    private final UserService userService;

    @GetMapping("/staff")
    public Result<List<UserVO>> getStaffUsers(){
        return Result.success(userService.getStaffUsers());
    }

    @PostMapping("/{id}/role")
    public Result<UserVO> updateUser(@PathVariable String id ,
                                     @Valid @RequestBody UserRoleUpdateDTO dto){
        return Result.success(userService.updateRole(id ,dto));
    }
}
