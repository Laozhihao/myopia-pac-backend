package com.wupol.myopia.business.core.screening.organization.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrganizationStaffQueryDTO;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationStaff;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 机构-人员表Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
public interface ScreeningOrganizationStaffMapper extends BaseMapper<ScreeningOrganizationStaff> {

    IPage<ScreeningOrganizationStaff> getByPage(@Param("page") Page<?> page, @Param("userIds") List<Integer> userIds,@Param("type") Integer type);

    List<ScreeningOrganizationStaff> getByOrgId(@Param("orgId") Integer orgId);

    List<ScreeningOrganizationStaff> getByOrgIds(@Param("orgIds") List<Integer> orgIds,@Param("type") Integer type);

    List<ScreeningOrganizationStaff> getByUserIds(@Param("userIds") List<Integer> userIds);

    ScreeningOrganizationStaff getByUserId(@Param("userId") Integer userId);
}
