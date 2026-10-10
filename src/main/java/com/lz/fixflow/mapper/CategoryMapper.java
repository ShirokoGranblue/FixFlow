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
        SELECT
            t.category_id AS categoryId,
            c.name AS categoryName,
            COUNT(*) AS count
        FROM ticket t
        LEFT JOIN category c
            ON t.category_id = c.id
        GROUP BY t.category_id, c.name
    """)
    List<Map<String,Object>> countCategory();
}
