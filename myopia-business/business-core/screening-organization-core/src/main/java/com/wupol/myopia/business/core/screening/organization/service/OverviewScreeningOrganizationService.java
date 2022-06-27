package com.wupol.myopia.business.core.screening.organization.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.screening.organization.domain.mapper.OverviewScreeningOrganizationMapper;
import com.wupol.myopia.business.core.screening.organization.domain.model.OverviewScreeningOrganization;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

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

    /**
     * 获取指定总览机构集所绑定的医院数量
     * @param overviewIds
     * @return
     */
    public Map<Integer, Long> getOverviewScreeningOrganizationNum(List<Integer> overviewIds) {
        if (CollectionUtils.isEmpty(overviewIds)) {
            return Collections.emptyMap();
        }
        return baseMapper.getListByOverviewIds(overviewIds).stream()
                .collect(Collectors.groupingBy(OverviewScreeningOrganization::getOverviewId, Collectors.counting()));
    }

    /**
     * 获取指定总览机构所绑定的医院id集
     * @param overviewId
     * @return
     */
    public List<Integer> getScreeningOrganizationIdByOverviewId(Integer overviewId) {
        return getByOverviewId(overviewId).stream().map(OverviewScreeningOrganization::getScreeningOrganizationId).collect(Collectors.toList());
    }

    /**
     * 获取指定总览机构的绑定信息
     * @param overviewId
     * @return
     */
    public List<OverviewScreeningOrganization> getByOverviewId(Integer overviewId) {
        return baseMapper.getListByOverviewIds(Arrays.asList(overviewId));
    }

}
