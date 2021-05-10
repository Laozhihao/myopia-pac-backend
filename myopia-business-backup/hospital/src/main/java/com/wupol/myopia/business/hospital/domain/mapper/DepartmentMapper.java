package com.wupol.myopia.business.hospital.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.hospital.domain.model.Department;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 医院-科室，Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
public interface DepartmentMapper extends BaseMapper<Department> {


    List<Department> getBy(Department query);

    IPage<Department> getByPage(@Param("page") Page<?> page, @Param("query") Department query);

}
