package com.wupol.myopia.business.core.common.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.common.domain.model.SystemUpdateNotice;
import org.apache.ibatis.annotations.Param;

/**
 * 系统更新表Mapper接口
 *
 * @Author Simple4H
 * @Date 2022-10-19
 */
public interface SystemUpdateNoticeMapper extends BaseMapper<SystemUpdateNotice> {

    SystemUpdateNotice getLastSystemUpdateNotice(@Param("systemCode") Integer systemCode);

}
