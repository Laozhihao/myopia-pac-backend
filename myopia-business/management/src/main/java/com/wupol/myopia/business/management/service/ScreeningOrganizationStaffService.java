package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.dto.OrganizationStaffRequest;
import com.wupol.myopia.business.management.domain.mapper.ScreeningOrganizationStaffMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganizationStaff;
import org.springframework.stereotype.Service;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Service
public class ScreeningOrganizationStaffService extends BaseService<ScreeningOrganizationStaffMapper, ScreeningOrganizationStaff> {

    public IPage<ScreeningOrganizationStaff> getOrganizationStaffList(OrganizationStaffRequest request) {

        Page<ScreeningOrganizationStaff> page = new Page<>(request.getPage(), request.getLimit());
        QueryWrapper<ScreeningOrganizationStaff> wrapper = new QueryWrapper<>();

        return baseMapper.selectPage(page, wrapper);
    }


}
