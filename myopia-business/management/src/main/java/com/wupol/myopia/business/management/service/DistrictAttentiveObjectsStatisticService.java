package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.DistrictAttentiveObjectsStatisticMapper;
import com.wupol.myopia.business.management.domain.model.DistrictAttentiveObjectsStatistic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Service
public class DistrictAttentiveObjectsStatisticService extends BaseService<DistrictAttentiveObjectsStatisticMapper, DistrictAttentiveObjectsStatistic> {

    @Autowired
    private DistrictService districtService;
    @Autowired
    private ScreeningTaskService screeningTaskService;

    /**
     * 获取统计数据
     *
     * @param districtIds
     * @return
     */
    public List<DistrictAttentiveObjectsStatistic> getStatisticDtoByDistrictIdAndTaskId(Set<Integer> districtIds) {
        LambdaQueryWrapper<DistrictAttentiveObjectsStatistic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DistrictAttentiveObjectsStatistic::getDistrictId, districtIds);
        List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics = baseMapper.selectList(queryWrapper);
        return districtAttentiveObjectsStatistics;
    }
}
