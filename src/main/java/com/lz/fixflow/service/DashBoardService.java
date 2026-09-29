package com.lz.fixflow.service;

import com.lz.fixflow.vo.DashBoardVO;
import org.springframework.stereotype.Service;

@Service
public interface DashBoardService {

    DashBoardVO getDashBoard();
}
