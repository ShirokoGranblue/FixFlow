package com.lz.fixflow.vo;


import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashBoardVO {

    private Long totalCount;

    private Long openCount;

    private Long inProgressCount;

    private Long resolvedCount;

    private Long closedCount;

    private Long todayCount;

    private List<Map<String, Object>> categoryCount;
}
