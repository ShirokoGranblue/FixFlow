package com.lz.fixflow.controller;

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
public class DashBoardController {

    private final DashBoardService dashBoardService;

    @GetMapping("/")
    public Result<DashBoardVO> getDashBoard() {
        return Result.success(dashBoardService.getDashBoard());
    }
}
