package com.wupol.myopia.business.management.domain.mapper;

import com.wupol.myopia.business.management.domain.model.ScreeningOrganizationStaff;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * 机构-人员表Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
public interface ScreeningOrganizationStaffMapper extends BaseMapper<ScreeningOrganizationStaff> {

    List<ScreeningOrganizationStaff> getByIds(List<Integer> ids);
}
