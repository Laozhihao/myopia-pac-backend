package com.wupol.myopia.base.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 基础业务类封装
 *
 * @Author HaoHao
 * @Date 2020/12/20
 **/
public abstract class BaseService<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {
    public final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 分页查询
     *
     * @param entity   查询实体参数
     * @param pageNum  页码
     * @param pageSize 页数
     * @return com.baomidou.mybatisplus.core.metadata.IPage<T>
     **/
    public IPage<T> findByPage(T entity, Integer pageNum, Integer pageSize) throws IOException {
        Page<T> page = new Page<>(pageNum, pageSize);
        return page(page, getQueryWrapper(entity).orderByDesc("id"));
    }

    /**
     * 查询List集合
     *
     * @param entity 查询实体参数
     * @return java.util.List<T>
     **/
    public List<T> findByList(T entity) throws IOException {
        return list(getQueryWrapper(entity));
    }

    /**
     * 查询List集合
     *
     * @param entity 查询实体参数
     * @return java.util.List<T>
     **/
    public List<T> findByList(T entity, String column, Boolean isDesc) throws IOException {
        if (StringUtils.isEmpty(column)){
            return list(getQueryWrapper(entity));
        }
        if (isDesc) return list(getQueryWrapper(entity).orderByDesc(column));
        return list(getQueryWrapper(entity).orderByAsc(column));
    }

    /**
     * 查询List集合
     *
     * @param entity 查询实体参数
     * @return java.util.List<T>
     **/
    public List<T> findByListOrderByIdDesc(T entity) throws IOException {
        return list(getQueryWrapper(entity).orderByDesc("id"));
    }

    /**
     * 根据多个条件获取一条记录
     *
     * @param entity 查询实体参数
     * @return T
     **/
    public T findOne(T entity) throws IOException {
        return getOne(getQueryWrapper(entity));
    }

    /**
     * 1、把实体类转为map，同时字段名变为下划线风格
     * 2、构造QueryWrapper，通过and连接条件，去除为null或""的字段
     *
     * @param entity 实体类
     * @return com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T>
     **/
    public QueryWrapper<T> getQueryWrapper(T entity) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE);
        Map<String, Object> params = mapper.readValue(mapper.writeValueAsString(entity), new TypeReference<Map<String, Object>>() {
        });
        return new QueryWrapper<T>().allEq((k, v) -> !StringUtils.isEmpty(v), params);
    }

    /**
     * 根据实体更新
     *
     * @param data  更新的数据
     * @param query 更新查询条件
     * @return java.lang.Boolean
     **/
    public Boolean update(T data, T query) throws IOException {
        return update(data, getQueryWrapper(query));
    }

    /**
     * 根据实体删除
     *
     * @param entity 删除条件
     * @return java.lang.Boolean
     **/
    public Boolean remove(T entity) throws IOException {
        return remove(getQueryWrapper(entity));
    }

    /**
     * 统计
     *
     * @param entity 统计条件
     * @return int
     **/
    public int count(T entity) throws IOException {
        return count(getQueryWrapper(entity));
    }
}
