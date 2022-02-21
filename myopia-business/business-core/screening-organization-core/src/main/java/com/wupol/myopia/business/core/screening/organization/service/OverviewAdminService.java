package com.wupol.myopia.business.core.screening.organization.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.screening.organization.domain.mapper.OverviewAdminMapper;
import com.wupol.myopia.business.core.screening.organization.domain.model.OverviewAdmin;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @Author wulizhou
 * @Date 2022-02-18
 */
@Service
public class OverviewAdminService extends BaseService<OverviewAdminMapper, OverviewAdmin> {

    /**
     * 新增总览机构管理员
     *
     * @param createUserId 创建人
     * @param overviewId   总览机构ID
     * @param userId       用户ID
     * @param govDeptId    部门ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveAdmin(Integer createUserId, Integer overviewId, Integer userId, Integer govDeptId) {
        OverviewAdmin overviewAdmin = new OverviewAdmin();
        overviewAdmin.setCreateUserId(createUserId);
        overviewAdmin.setOverviewId(overviewId);
        overviewAdmin.setUserId(userId);
        overviewAdmin.setGovDeptId(govDeptId);
        baseMapper.insert(overviewAdmin);
    }


}
