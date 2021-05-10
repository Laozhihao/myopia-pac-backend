package com.wupol.myopia.business.management.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.ScreeningOrganizationAdminMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganizationAdmin;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 筛查机构管理员
 *
 * @author Simple4H
 */
@Service
@Log4j2
public class ScreeningOrganizationAdminService extends BaseService<ScreeningOrganizationAdminMapper, ScreeningOrganizationAdmin> {
    /**
     * 新增筛查机构管理员
     *
     * @param createUserId   创建人
     * @param screeningOrgId 筛查机构ID
     * @param userId         用户ID
     * @param govDeptId      部门ID
     * @return 新增个数
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertAdmin(Integer createUserId, Integer screeningOrgId, Integer userId, Integer govDeptId) {
        ScreeningOrganizationAdmin organizationAdmin = new ScreeningOrganizationAdmin();
        organizationAdmin.setCreateUserId(createUserId);
        organizationAdmin.setScreeningOrgId(screeningOrgId);
        organizationAdmin.setUserId(userId);
        organizationAdmin.setGovDeptId(govDeptId);
        baseMapper.insert(organizationAdmin);
    }

    /**
     * 通过筛查ID获取筛查ADMIN
     *
     * @param orgId 筛查机构ID
     * @return admin
     */
    public ScreeningOrganizationAdmin getByOrgId(Integer orgId) {
        return baseMapper.getByOrgId(orgId);
    }

    /**
     * 通过筛查ID获取筛查ADMIN
     *
     * @param orgIds 筛查机构Ids
     * @return List<ScreeningOrganizationAdmin>
     */
    public List<ScreeningOrganizationAdmin> getByOrgIds(List<Integer> orgIds) {
        return baseMapper.getByOrgIds(orgIds);
    }
}
