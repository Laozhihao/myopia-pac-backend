package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.dto.OrganizationStaffRequest;
import com.wupol.myopia.business.management.domain.mapper.ScreeningOrganizationStaffMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganization;
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
    private GovDeptService govDeptService;

    @Resource
    private ScreeningOrganizationService screeningOrganizationService;

    /**
     * 获取机构人员列表
     *
     * @param request   请求入参
     * @param govDeptId 部门id
     * @return Page<ScreeningOrganizationStaff> {@link Page}
     */
    public Page<ScreeningOrganizationStaff> getOrganizationStaffList(OrganizationStaffRequest request, Integer govDeptId) {

        Page<ScreeningOrganizationStaff> page = new Page<>(request.getCurrent(), request.getSize());
        QueryWrapper<ScreeningOrganizationStaff> wrapper = new QueryWrapper<>();

        likeQueryAppend(wrapper, "screening_org_id", request.getScreeningOrgId());
        InQueryAppend(wrapper, "gov_dept_id", govDeptService.getAllSubordinate(govDeptId));

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

    /**
     * 删除用户
     *
     * @param id           id
     * @param createUserId 创建人
     * @return 删除个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer deletedOrganizationStaff(Integer id, Integer createUserId) {
        // TODO: 删除用户
        return 1;
    }

    /**
     * 新增员工
     *
     * @param screeningOrganizationStaff 员工实体类
     * @return 新增个数
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized Integer saveOrganizationStaff(ScreeningOrganizationStaff screeningOrganizationStaff) {
        // 通过screeningOrgId获取机构
        ScreeningOrganization organization = screeningOrganizationService.getById(screeningOrganizationStaff.getScreeningOrgId());
        screeningOrganizationStaff.setStaffNo(generateOrgNo(organization.getOrgNo(), Const.STAFF_ID_CARD));
        screeningOrganizationStaff.setUserId(Const.STAFF_USER_ID);
        generateAccountAndPassword();
        return baseMapper.insert(screeningOrganizationStaff);
    }

    /**
     * 更新员工
     *
     * @param screeningOrganizationStaff 员工实体类
     * @return 更新个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer updateOrganizationStaff(ScreeningOrganizationStaff screeningOrganizationStaff) {
        return baseMapper.updateById(screeningOrganizationStaff);
    }

    /**
     * 生成账号密码
     */
    private void generateAccountAndPassword() {

    }

    private String generateOrgNo(String orgNo, String idCard) {
        return StringUtils.join(orgNo, StringUtils.right(idCard, 6));
    }
}
