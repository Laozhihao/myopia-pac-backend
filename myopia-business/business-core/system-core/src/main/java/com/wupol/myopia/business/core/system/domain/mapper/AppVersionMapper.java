package com.wupol.myopia.business.core.system.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.system.domain.model.AppVersion;

/**
 * APP版本管理表Mapper接口
 *
 * @Author HaoHao
 * @Date 2021-11-23
 */
public interface AppVersionMapper extends BaseMapper<AppVersion> {

    /**
     * 获取最新的一个版本
     *
     * @param appVersion 查询条件
     * @return com.wupol.myopia.business.core.system.domain.model.AppVersion
     **/
    AppVersion selectLatestVersionByPackageNameAndChannel(AppVersion appVersion);

}
