package com.lz.fixflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lz.fixflow.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
