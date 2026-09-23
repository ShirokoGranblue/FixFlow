package com.lz.fixflow.service;

import com.lz.fixflow.vo.CategoryVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {

    List<CategoryVO> getCategories();
}
