package com.lz.fixflow.cache;

import com.lz.fixflow.vo.DashBoardVO;

import tools.jackson.core.type.TypeReference;

public final class DashboardCache {
    private DashboardCache(){
    }

    public static final String KEY = "fixflow:dashboard:stats:v1";
    public static final TypeReference<DashBoardVO> TYPE = new TypeReference<DashBoardVO>() {};
}
