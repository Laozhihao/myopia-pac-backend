package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.management.domain.dto.ScreeningOrgResponseDTO;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.management.domain.query.ScreeningOrganizationQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 筛查机构表Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
public interface ScreeningOrganizationMapper extends BaseMapper<ScreeningOrganization> {

    IPage<ScreeningOrgResponseDTO> getScreeningOrganizationListByCondition(@Param("page") Page<?> page, @Param("name") String name,
                                                                           @Param("type") Integer type, @Param("configType") Integer configType,
                                                                           @Param("districtId") Integer districtId, @Param("govDeptId") Integer govDeptId,
                                                                           @Param("phone") String phone, @Param("status") Integer status);

    ScreeningOrganization getLastOrgByNo(@Param("code") Integer code);

    List<ScreeningOrganization> getBy(ScreeningOrganizationQuery query);

    ScreeningOrganization getLastOrgByNo(@Param("code") Long code);

    ScreeningOrgResponseDTO getOrgById(@Param("id") Integer id);

    IPage<ScreeningOrganization> getByPage(@Param("page") Page<?> page, @Param("screeningOrganizationQuery") ScreeningOrganizationQuery screeningOrganizationQuery);
}
