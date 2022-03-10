package com.wupol.myopia.business.core.screening.organization.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.screening.organization.domain.mapper.OverviewHospitalMapper;
import com.wupol.myopia.business.core.screening.organization.domain.model.OverviewHospital;
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
public class OverviewHospitalService extends BaseService<OverviewHospitalMapper, OverviewHospital> {

    /**
     * 批量插入总览机构医院绑定信息
     * @param overviewId
     * @param hospitalIds
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(Integer overviewId, List<Integer> hospitalIds) {
        if (Objects.isNull(overviewId) || CollectionUtils.isEmpty(hospitalIds)) {
            return 0;
        }
        return baseMapper.batchSave(overviewId, hospitalIds);
    }

    /**
     * 更新绑定信息
     * @param overviewId
     * @param hospitalIds
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateBindInfo(Integer overviewId, List<Integer> hospitalIds) {
        // 先删除再绑定
        if (Objects.isNull(overviewId)) {
            return 0;
        }
        super.remove(new OverviewHospital().setOverviewId(overviewId));
        if (CollectionUtils.isEmpty(hospitalIds)) {
            return 0;
        }
        return baseMapper.batchSave(overviewId, hospitalIds);
    }

    /**
     * 获取指定总览机构集所绑定的医院数量
     * @param overviewIds
     * @return
     */
    public Map<Integer, Long> getOverviewHospitalNum(List<Integer> overviewIds) {
        if (CollectionUtils.isEmpty(overviewIds)) {
            return Collections.EMPTY_MAP;
        }
        return baseMapper.getListByOverviewIds(overviewIds).stream()
                .collect(Collectors.groupingBy(OverviewHospital::getOverviewId, Collectors.counting()));
    }

    /**
     * 获取指定总览机构所绑定的医院id集
     * @param overviewId
     * @return
     */
    public List<Integer> getHospitalIdByOverviewId(Integer overviewId) {
        return getByOverviewId(overviewId).stream().map(OverviewHospital::getHospitalId).collect(Collectors.toList());
    }

    /**
     * 获取指定总览机构的绑定信息
     * @param overviewId
     * @return
     */
    public List<OverviewHospital> getByOverviewId(Integer overviewId) {
        return baseMapper.getListByOverviewIds(Arrays.asList(overviewId));
    }

}
