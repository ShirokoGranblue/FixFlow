package com.lz.fixflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lz.fixflow.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    @Select("""
            SELECT category_id , COUNT(*) as count
            FROM ticket
            GROUP BY category_id
            """)
    List<Map<String,Object>> countCategory();
}
