package com.wupol.myopia.migrate.domain.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.wupol.myopia.migrate.domain.model.SysGradeClass;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * Mapper接口
 *
 * @Author HaoHao
 * @Date 2022-03-23
 */
@DS("data_source_db")
public interface SysGradeClassMapper extends BaseMapper<SysGradeClass> {

}
