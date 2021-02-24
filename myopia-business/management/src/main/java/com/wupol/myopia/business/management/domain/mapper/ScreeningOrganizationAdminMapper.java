package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganizationAdmin;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 筛查机构-管理员Mapper接口
 *
 * @author Simple4H
 */
public interface ScreeningOrganizationAdminMapper extends BaseMapper<ScreeningOrganizationAdmin> {

    List<ScreeningOrganizationAdmin> getByOrgIds(@Param("orgIds") List<Integer> orgIds);
}
