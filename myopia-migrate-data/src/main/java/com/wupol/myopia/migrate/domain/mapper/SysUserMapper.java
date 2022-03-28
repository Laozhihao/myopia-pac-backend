package com.wupol.myopia.migrate.domain.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.wupol.myopia.migrate.domain.model.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 管理员表Mapper接口
 *
 * @Author HaoHao
 * @Date 2022-03-23
 */
@DS("data_source_db")
public interface SysUserMapper extends BaseMapper<SysUser> {

}
