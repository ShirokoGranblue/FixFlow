package com.lz.fixflow.cache;

import java.util.List;
import com.lz.fixflow.vo.CategoryVO;
import tools.jackson.core.type.TypeReference;

public final class CategoryCache {
    private CategoryCache(){
    }
    public final static String KEY = "fixflow:category:list:v1";
    public static final TypeReference<List<CategoryVO>> TYPE = new TypeReference<List<CategoryVO>>() {};
}
