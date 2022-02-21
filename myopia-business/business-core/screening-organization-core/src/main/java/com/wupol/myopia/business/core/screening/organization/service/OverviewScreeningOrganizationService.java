package com.wupol.myopia.business.core.screening.organization.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.screening.organization.domain.mapper.OverviewScreeningOrganizationMapper;
import com.wupol.myopia.business.core.screening.organization.domain.model.OverviewScreeningOrganization;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * @Author wulizhou
 * @Date 2022-02-17
 */
@Service
public class OverviewScreeningOrganizationService extends BaseService<OverviewScreeningOrganizationMapper, OverviewScreeningOrganization> {

    /**
     * 批量插入总览机构筛查机构绑定信息
     * @param overviewId
     * @param screeningOrganizationIds
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(Integer overviewId, List<Integer> screeningOrganizationIds) {
        if (Objects.isNull(overviewId) || CollectionUtils.isEmpty(screeningOrganizationIds)) {
            return 0;
        }
        return baseMapper.batchSave(overviewId, screeningOrganizationIds);
    }

    /**
     * 更新绑定信息
     * @param overviewId
     * @param screeningOrganizationIds
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateBindInfo(Integer overviewId, List<Integer> screeningOrganizationIds) {
        // 先删除再绑定
        if (Objects.isNull(overviewId)) {
            return 0;
        }
        super.remove(new OverviewScreeningOrganization().setOverviewId(overviewId));
        if (CollectionUtils.isEmpty(screeningOrganizationIds)) {
            return 0;
        }
        return baseMapper.batchSave(overviewId, screeningOrganizationIds);
    }

}
