package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.DistrictMonitorStatisticMapper;
import com.wupol.myopia.business.management.domain.model.DistrictMonitorStatistic;
import com.wupol.myopia.business.management.domain.model.DistrictVisionStatistic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Service
public class DistrictMonitorStatisticService extends BaseService<DistrictMonitorStatisticMapper, DistrictMonitorStatistic> {
    @Autowired
    private DistrictService districtService;

    /**
     * 获取数据
     *
     * @param noticeId
     * @param currentDistrictId
     * @param user
     * @param istotal
     * @return
     * @throws IOException
     */
    public List<DistrictMonitorStatistic> getStatisticDtoByNoticeIdAndCurrentDistrictId(Integer noticeId, Integer currentDistrictId, CurrentUser user, boolean istotal) throws IOException {
        if (noticeId == null || user == null) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<DistrictMonitorStatistic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DistrictMonitorStatistic::getScreeningNoticeId, noticeId);
        queryWrapper.eq(DistrictMonitorStatistic::getIsTotal, istotal);
        queryWrapper.eq(DistrictMonitorStatistic::getDistrictId, currentDistrictId);
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 获取数据
     *
     * @param noticeId
     * @param currentDistrictId
     * @param user
     * @param istotal
     * @return
     * @throws IOException
     */
    public List<DistrictMonitorStatistic> getStatisticDtoByNoticeIdAndCurrentChildDistrictIds(Integer noticeId, Integer currentDistrictId, CurrentUser user, boolean istotal) throws IOException {
        if (noticeId == null || user == null) {
            return new ArrayList<>();
        }
        List<DistrictMonitorStatistic> districtMonitorStatistics = new ArrayList<>();
        Set<Integer> districtIds = districtService.getChildDistrictIdsByDistrictId(currentDistrictId);
        districtIds.add(currentDistrictId);
        Lists.partition(new ArrayList<>(districtIds), 100).forEach(districtIdList -> {
            LambdaQueryWrapper<DistrictMonitorStatistic> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DistrictMonitorStatistic::getScreeningNoticeId, noticeId);
            queryWrapper.eq(DistrictMonitorStatistic::getIsTotal, istotal);
            queryWrapper.in(DistrictMonitorStatistic::getDistrictId, districtIdList);
            districtMonitorStatistics.addAll(baseMapper.selectList(queryWrapper));
        });
        return districtMonitorStatistics;
    }

    /**
     * 根据唯一索引批量新增或更新
     *
     * @param districtMonitorStatistics
     */
    public void batchSaveOrUpdate(List<DistrictMonitorStatistic> districtMonitorStatistics) {
        if (CollectionUtils.isEmpty(districtMonitorStatistics)) {
            return;
        }
        Lists.partition(districtMonitorStatistics, 20).forEach(statistics -> baseMapper.batchSaveOrUpdate(statistics));
    }
}
