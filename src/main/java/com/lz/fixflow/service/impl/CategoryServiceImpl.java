package com.lz.fixflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lz.fixflow.common.BusinessException;
import com.lz.fixflow.entity.Category;
import com.lz.fixflow.mapper.CategoryMapper;
import com.lz.fixflow.service.CategoryService;
import com.lz.fixflow.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    private CategoryVO convertToVO(Category entity) {

        CategoryVO vo = new CategoryVO();

        if (entity == null ){
            throw new BusinessException("内部错误");
        }

        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setCreatedAt(entity.getCreatedAt());

        return vo;
    }

    @Override
    public List<CategoryVO> getCategories(){
        String userid = StpUtil.getLoginIdAsString();

        List<Category> list = categoryMapper.selectList(null);

        return list.stream()
                .map(this::convertToVO)
                .toList();
    }
}
