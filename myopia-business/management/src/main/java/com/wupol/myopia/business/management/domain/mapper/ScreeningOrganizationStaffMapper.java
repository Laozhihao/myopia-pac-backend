package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganizationStaff;
import com.wupol.myopia.business.management.domain.query.ScreeningOrganizationStaffQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 机构-人员表Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
public interface ScreeningOrganizationStaffMapper extends BaseMapper<ScreeningOrganizationStaff> {

    IPage<ScreeningOrganizationStaff> getByPage(@Param("page") Page<?> page, @Param("screeningOrganizationStaffQuery") ScreeningOrganizationStaffQuery screeningOrganizationStaffQuery);

    List<ScreeningOrganizationStaff> getByOrgId(@Param("orgId") Integer orgId);

    List<ScreeningOrganizationStaff> getByOrgIds(@Param("orgIds") List<Integer> orgIds);

    List<ScreeningOrganizationStaff> getByUserIds(@Param("userIds") List<Integer> userIds);
}
