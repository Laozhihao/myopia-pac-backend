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

    List<ScreeningOrganizationStaff> getByIds(List<Integer> ids);

    IPage<ScreeningOrganizationStaff> getByPage(@Param("page") Page<?> page, @Param("screeningOrganizationStaffQuery") ScreeningOrganizationStaffQuery screeningOrganizationStaffQuery);
}
