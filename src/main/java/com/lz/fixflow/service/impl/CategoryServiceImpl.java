package com.lz.fixflow.service.impl;

import com.lz.fixflow.cache.CategoryCache;
import com.lz.fixflow.entity.Category;
import com.lz.fixflow.mapper.CategoryMapper;
import com.lz.fixflow.service.CategoryService;
import com.lz.fixflow.vo.CategoryVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    private static final Duration TTL = Duration.ofMinutes(5);
    private final StringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;

    private CategoryVO convertToVO(Category entity) {

        CategoryVO vo = new CategoryVO();

        if (entity == null ){
            throw new IllegalStateException("内部错误");
        }

        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setCreatedAt(entity.getCreatedAt());

        return vo;
    }

    @Override
    public List<CategoryVO> getCategories(){

        String cachedJson = getCachedJson();
        if(StringUtils.hasText(cachedJson)){
            try{
                return jsonMapper.readValue(cachedJson, CategoryCache.TYPE);
            }catch(JacksonException e){
                log.warn("缓存反序列化失败，准备重新加载",e);
                deleteCache();
            }
        }
        List<CategoryVO> categories = loadFromDB();

        saveCache(categories);

        return categories;
    }

    private String getCachedJson(){
        try{
            return redisTemplate.opsForValue().get(CategoryCache.KEY);
        }catch(DataAccessException e){
            log.warn("Redis 命中失败，降级查询 MySQL",e);
            return null;
        }
    }
    private void deleteCache(){
        try{
            redisTemplate.delete(CategoryCache.KEY);
        }catch(DataAccessException e){
            log.warn("缓存删除失败",e);
        }
    }
    private void saveCache(List<CategoryVO> categories){
        try{
            String value = jsonMapper.writeValueAsString(categories);
            redisTemplate.opsForValue().set(CategoryCache.KEY, value, TTL);
        }catch(JacksonException | DataAccessException e){
            log.warn("缓存写入失败",e);
        }
    }
    private List<CategoryVO> loadFromDB(){

        List<Category> categories = categoryMapper.selectList(null);
        
        return categories.stream()
                .map(this::convertToVO)
                .toList();
    }
}
