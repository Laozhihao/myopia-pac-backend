package com.wupol.myopia.business.core.stat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.stat.domain.mapper.DistrictAttentiveObjectsStatisticMapper;
import com.wupol.myopia.business.core.stat.domain.model.DistrictAttentiveObjectsStatistic;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 地区重点对象统计服务层
 *
 * @Author jacob
 * @Date 2021-01-20
 */
@Service
public class DistrictAttentiveObjectsStatisticService extends BaseService<DistrictAttentiveObjectsStatisticMapper, DistrictAttentiveObjectsStatistic> {

    /**
     * 获取统计数据
     *
     * @param currentDistrictId
     * @param isTotal
     * @return
     */
    public List<DistrictAttentiveObjectsStatistic> getStatisticDtoByCurrentDistrictIdAndTaskId(Integer currentDistrictId, Boolean isTotal) {
        LambdaQueryWrapper<DistrictAttentiveObjectsStatistic> queryWrapper = new LambdaQueryWrapper<>();
        if (Objects.nonNull(isTotal)) {
            queryWrapper.eq(DistrictAttentiveObjectsStatistic::getIsTotal, isTotal);
        }
        queryWrapper.eq(DistrictAttentiveObjectsStatistic::getDistrictId, currentDistrictId);
        queryWrapper.orderByDesc(DistrictAttentiveObjectsStatistic::getUpdateTime);
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 获取统计数据
     *
     * @param districtIds
     * @param isTotal
     * @return
     */
    public List<DistrictAttentiveObjectsStatistic> getStatisticDtoByDistrictIdsAndTaskId(Set<Integer> districtIds, Boolean isTotal) {
        List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics = new ArrayList<>();
        Lists.partition(new ArrayList<>(districtIds), 100).forEach(districtIdList -> {
            LambdaQueryWrapper<DistrictAttentiveObjectsStatistic> queryWrapper = new LambdaQueryWrapper<>();
            if (Objects.nonNull(isTotal)) {
                queryWrapper.eq(DistrictAttentiveObjectsStatistic::getIsTotal, isTotal);
            }
            queryWrapper.in(DistrictAttentiveObjectsStatistic::getDistrictId, districtIdList);
            queryWrapper.orderByDesc(DistrictAttentiveObjectsStatistic::getUpdateTime);
            districtAttentiveObjectsStatistics.addAll(baseMapper.selectList(queryWrapper));
        });
        return districtAttentiveObjectsStatistics;
    }

    /**
     * 根据唯一索引批量新增或更新
     * @param districtAttentiveObjectsStatistics
     */
    public void batchSaveOrUpdate(List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics) {
        if (CollectionUtils.isEmpty(districtAttentiveObjectsStatistics)) {
            return;
        }
        Lists.partition(districtAttentiveObjectsStatistics, 20).forEach(statistics -> baseMapper.batchSaveOrUpdate(statistics));
    }
}
