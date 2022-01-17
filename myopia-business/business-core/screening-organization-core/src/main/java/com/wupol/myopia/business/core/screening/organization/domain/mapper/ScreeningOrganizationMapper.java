package com.wupol.myopia.business.core.screening.organization.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrgResponseDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrganizationQueryDTO;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
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
                                                                           @Param("phone") String phone, @Param("status") Integer status,@Param("cooperationType") Integer cooperationType,
                                                                           @Param("startTimes") String startTimes,@Param("endTimes") String endTimes);

    List<ScreeningOrganization> getBy(ScreeningOrganizationQueryDTO query);

    ScreeningOrgResponseDTO getOrgById(@Param("id") Integer id);

    IPage<ScreeningOrganization> getByPage(@Param("page") Page<?> page, @Param("screeningOrganizationQueryDTO") ScreeningOrganizationQueryDTO screeningOrganizationQueryDTO);

    List<ScreeningOrganization> getByNameAndNeId(@Param("name") String name, @Param("id") Integer id);

    List<ScreeningOrganization> getByName(@Param("name") String name);

    List<ScreeningOrganization> getListByProvinceCodeAndNameLike(@Param("name") String name, @Param("provinceDistrictCode") Long provinceDistrictCode);

    List<ScreeningOrganization> getByConfigType(@Param("configType") Integer configType);

    List<ScreeningOrganization> getAll();

    List<ScreeningOrgResponseDTO> getOrgByIds(@Param("ids") List<Integer> ids);

    List<ScreeningOrganization> getByCooperationTimeAndStatus(@Param("date") Date date);

    int updateOrganizationStatus(@Param("id") Integer id, @Param("targetStatus") Integer targetStatus, @Param("sourceStatus")Integer sourceStatus);

    List<ScreeningOrganization> getByCooperationEndTime(@Param("start") Date start, @Param("end") Date end);

}
