package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.DistrictAttentiveObjectsStatisticMapper;
import com.wupol.myopia.business.management.domain.model.DistrictAttentiveObjectsStatistic;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 地区重点对象统计服务层
 *
 * @Author jacob
 * @Date 2021-01-20
 */
@Service
public class DistrictAttentiveObjectsStatisticService extends BaseService<DistrictAttentiveObjectsStatisticMapper, DistrictAttentiveObjectsStatistic> {

    @Autowired
    private DistrictService districtService;

    /**
     * 获取统计数据
     *
     * @param districtIds
     * @return
     */
    public List<DistrictAttentiveObjectsStatistic> getStatisticDtoByDistrictIdAndTaskId(Set<Integer> districtIds,  Integer currentDistrictId, Boolean isTotal,boolean isCurrent) {
        LambdaQueryWrapper<DistrictAttentiveObjectsStatistic> queryWrapper = new LambdaQueryWrapper<>();
        if (Objects.nonNull(isTotal)) {
            queryWrapper.eq(DistrictAttentiveObjectsStatistic::getIsTotal,isTotal);
        }
        if (isCurrent) {
            queryWrapper.eq(DistrictAttentiveObjectsStatistic::getDistrictId,currentDistrictId);
        } else {
            queryWrapper.in(DistrictAttentiveObjectsStatistic::getDistrictId, districtIds);
        }
        queryWrapper.orderByDesc(DistrictAttentiveObjectsStatistic::getUpdateTime);
        List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics = baseMapper.selectList(queryWrapper);
        return districtAttentiveObjectsStatistics;
    }

    /**
     * 通过用户获取user
     *
     * @param user
     * @return
     */
    public List<DistrictAttentiveObjectsStatistic> getDataByUser(CurrentUser user) throws IOException {
        LambdaQueryWrapper<DistrictAttentiveObjectsStatistic> queryWrapper = new LambdaQueryWrapper<>();
        // 调整为根据districtId获取
        queryWrapper.in(DistrictAttentiveObjectsStatistic::getDistrictId, districtService.getCurrentUserDistrictTreeAllIds(user));
        // 查找所有数据
        List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics = baseMapper.selectList(queryWrapper);
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
        baseMapper.batchSaveOrUpdate(districtAttentiveObjectsStatistics);
    }
}
