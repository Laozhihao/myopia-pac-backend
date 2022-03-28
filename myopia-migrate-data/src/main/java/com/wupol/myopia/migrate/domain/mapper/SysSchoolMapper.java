package com.wupol.myopia.migrate.domain.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.wupol.myopia.migrate.domain.model.SysSchool;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 学校表Mapper接口
 *
 * @Author HaoHao
 * @Date 2022-03-23
 */
@DS("data_source_db")
public interface SysSchoolMapper extends BaseMapper<SysSchool> {

}
