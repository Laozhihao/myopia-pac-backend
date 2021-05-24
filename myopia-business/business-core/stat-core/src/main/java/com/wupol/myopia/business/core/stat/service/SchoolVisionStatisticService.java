package com.wupol.myopia.business.core.stat.service;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.stat.domain.mapper.SchoolVisionStatisticMapper;
import com.wupol.myopia.business.core.stat.domain.model.SchoolVisionStatistic;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Service
public class SchoolVisionStatisticService extends BaseService<SchoolVisionStatisticMapper, SchoolVisionStatistic> {

    /**
     * 通过planId、学校ID获取列表
     *
     * @param planIds  planIds
     * @param schoolId 学校ID
     * @return List<SchoolVisionStatistic>
     */
    public List<SchoolVisionStatistic> getByPlanIdsAndSchoolId(List<Integer> planIds, Integer schoolId) {
        return baseMapper.getByPlanIdsAndSchoolId(planIds, schoolId);
    }

    /**
     * 根据唯一索引批量新增或更新
     *
     * @param schoolVisionStatistics 学校某次筛查计划统计视力情况
     */
    public void batchSaveOrUpdate(List<SchoolVisionStatistic> schoolVisionStatistics) {
        if (CollectionUtils.isEmpty(schoolVisionStatistics)) {
            return;
        }
        Lists.partition(schoolVisionStatistics, 20).forEach(statistics -> baseMapper.batchSaveOrUpdate(statistics));
    }
}
