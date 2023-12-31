package com.wupol.myopia.business.core.stat.service;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.stat.domain.mapper.SchoolMonitorStatisticMapper;
import com.wupol.myopia.business.core.stat.domain.model.SchoolMonitorStatistic;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2021-02-24
 */
@Service
public class SchoolMonitorStatisticService extends BaseService<SchoolMonitorStatisticMapper, SchoolMonitorStatistic> {

    /**
     * 根据唯一索引批量新增或更新
     *
     * @param schoolMonitorStatistics
     */
    public void batchSaveOrUpdate(List<SchoolMonitorStatistic> schoolMonitorStatistics) {
        if (CollectionUtils.isEmpty(schoolMonitorStatistics)) {
            return;
        }
        Lists.partition(schoolMonitorStatistics, 20).forEach(statistics -> baseMapper.batchSaveOrUpdate(statistics));
    }

    /**
     * 通过学校Id获取统计
     *
     * @param schoolId 学校Id
     * @return List<SchoolMonitorStatistic>
     */
    public List<SchoolMonitorStatistic> getBySchoolId(Integer schoolId) {
        return baseMapper.getBySchoolId(schoolId);
    }

    public void deleteByPlanId(Integer planId) {
        remove(new SchoolMonitorStatistic().setScreeningPlanId(planId));
    }
}
