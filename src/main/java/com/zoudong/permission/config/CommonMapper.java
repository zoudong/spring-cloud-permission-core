package com.zoudong.permission.config;

import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

/**
 * @author zd
 * @description class
 * @date 2018/6/13 17:27
 */
public interface CommonMapper<T> extends Mapper<T>, MySqlMapper<T> {
}
