package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.business.management.domain.model.DistrictVisionStatistic;
import com.wupol.myopia.business.management.domain.model.DistrictVisionStatistic;
import com.wupol.myopia.business.management.domain.mapper.DistrictVisionStatisticMapper;
import com.wupol.myopia.base.service.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Service
public class DistrictVisionStatisticService extends BaseService<DistrictVisionStatisticMapper, DistrictVisionStatistic> {

    public List<DistrictVisionStatistic> getStatisticDtoByDistrictIdAndTaskId(Set<Integer> districtIds, Long taskId) {
        LambdaQueryWrapper<DistrictVisionStatistic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DistrictVisionStatistic::getDistrictId, districtIds).eq(DistrictVisionStatistic::getScreeningTaskId, taskId);
        List<DistrictVisionStatistic> DistrictVisionStatistics = baseMapper.selectList(queryWrapper);
        return DistrictVisionStatistics;
    }
}
