package com.lz.fixflow.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.lz.fixflow.common.Result;
import com.lz.fixflow.service.DashBoardService;
import com.lz.fixflow.vo.DashBoardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
@SaCheckRole(value={"STAFF","ADMIN"}, mode = SaMode.OR)
public class DashBoardController {

    private final DashBoardService dashBoardService;

    @GetMapping("/")
    public Result<DashBoardVO> getDashBoard() {
        return Result.success(dashBoardService.getDashBoard());
    }
}
