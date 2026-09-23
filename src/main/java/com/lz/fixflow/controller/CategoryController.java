package com.lz.fixflow.controller;

import com.lz.fixflow.common.Result;
import com.lz.fixflow.service.CategoryService;
import com.lz.fixflow.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/")
    public Result<List<CategoryVO>> getCategories() {
        return Result.success(categoryService.getCategories());
    }
}
