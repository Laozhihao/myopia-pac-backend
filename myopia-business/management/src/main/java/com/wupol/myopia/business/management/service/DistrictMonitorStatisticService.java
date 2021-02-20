package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.DistrictMonitorStatisticMapper;
import com.wupol.myopia.business.management.domain.model.DistrictMonitorStatistic;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Service
public class DistrictMonitorStatisticService extends BaseService<DistrictMonitorStatisticMapper, DistrictMonitorStatistic> {

    public List<DistrictMonitorStatistic> getStatisticDtoByDistrictIdAndTaskId(Set<Integer> districtIds, Long taskId) {
        LambdaQueryWrapper<DistrictMonitorStatistic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DistrictMonitorStatistic::getDistrictId, districtIds).eq(DistrictMonitorStatistic::getScreeningTaskId, taskId);
        List<DistrictMonitorStatistic> districtMonitorStatistics = baseMapper.selectList(queryWrapper);
        return districtMonitorStatistics;
    }
}
