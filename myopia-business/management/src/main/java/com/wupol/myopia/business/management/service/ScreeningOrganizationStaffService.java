package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.dto.OrganizationStaffRequest;
import com.wupol.myopia.business.management.domain.mapper.ScreeningOrganizationStaffMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganizationStaff;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Service
public class ScreeningOrganizationStaffService extends BaseService<ScreeningOrganizationStaffMapper, ScreeningOrganizationStaff> {

    @Resource
    private HospitalService hospitalService;

    public IPage<ScreeningOrganizationStaff> getOrganizationStaffList(OrganizationStaffRequest request, Integer govDeptId) {

        Page<ScreeningOrganizationStaff> page = new Page<>(request.getPage(), request.getLimit());
        QueryWrapper<ScreeningOrganizationStaff> wrapper = new QueryWrapper<>();

        wrapper.like("screening_org_id", request.getScreeningOrgId())
                .in("gov_dept_id", hospitalService.getAllDeptId(govDeptId));

        if (StringUtils.isNotBlank(request.getName())) {

        }
        if (null != request.getIdCard()) {

        }
        if (null != request.getIdCard()) {

        }
        if (StringUtils.isNotBlank(request.getMobile())) {

        }
        return baseMapper.selectPage(page, wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer deletedOrganizationStaff(Integer id, Integer createUserId) {
        // TODO: 删除用户
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer saveOrganizationStaff(ScreeningOrganizationStaff screeningOrganizationStaff) {
        screeningOrganizationStaff.setStaffNo(generateStaffNo());
        screeningOrganizationStaff.setUserId(Const.STAFF_USER_ID);
        generateAccountAndPassword();
        return baseMapper.insert(screeningOrganizationStaff);
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer updateOrganizationStaff(ScreeningOrganizationStaff screeningOrganizationStaff) {
        return baseMapper.updateById(screeningOrganizationStaff);
    }

    /**
     * 生成账号密码
     */
    private void generateAccountAndPassword() {

    }

    /**
     * 生成编号
     *
     * @return Long
     */
    private Long generateStaffNo() {
        return 123L;
    }


}
