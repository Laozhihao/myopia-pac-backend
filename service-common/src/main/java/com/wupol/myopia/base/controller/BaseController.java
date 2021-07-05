package com.wupol.myopia.base.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 基础Controller
 *
 * @Author HaoHao
 * @Date 22020/12/20
 **/
public abstract class BaseController<M extends BaseService, T> {
	public final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected M baseService;

	/**
	 * 分页查询
	 *
	 * @param entity 查询参数
	 * @param pageNum 页码
	 * @param pageSize 条数
	 * @return Object
	 */
	@GetMapping("page")
    public IPage queryInfo(T entity,
                           @RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize) {
		return baseService.findByPage(entity, pageNum, pageSize);
    }

	/**
	 * 根据ID获取单个信息（这里默认所有表的主键字段都为“id”,且自增）
     *
	 * @param id ID
	 * @return Object
	 */
	@GetMapping("{id}")
    public Object getInfo(@PathVariable Integer id) {
		return baseService.getById(id);
    }

	/**
	 * 查询list集合
     *
	 * @param entity 查询参数
	 * @return Object
	 */
	@GetMapping("/list")
    public Object getList(T entity) {
        return baseService.findByList(entity);
    }

	/**
	 * 查询单个
     *
	 * @param entity 查询参数
	 * @return Object
	 */
	@GetMapping("detail")
	public Object getOne(T entity) {
		return baseService.findOne(entity);
	}

	/**
	 * 新增
     *
	 * @param entity 新增参数
	 * @return Object
	 */
	@PostMapping()
	public void createInfo(@RequestBody T entity) {
		if (!baseService.save(entity)) {
            throw new BusinessException("创建失败");
        }
	}
	
	/**
	 * 根据ID更新
	 * @param entity 更新参数
	 * @return Object
	 */
	@PutMapping()
	public void updateInfo(@RequestBody T entity) {
		if (!baseService.updateById(entity)) {
            throw new BusinessException("修改失败");
        }
	}
	
	/**
	 * 根据ID删除（这里默认所有表的主键字段都为“id”,且自增）
	 * @param id ID
	 * @return void
	 */
	@DeleteMapping("{id}")
	public void deleteInfo(@PathVariable Integer id) {
	    // 无论是否删除成功都是返回false
        baseService.removeById(id);
	}
	
}