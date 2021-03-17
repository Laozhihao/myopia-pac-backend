package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

    public List<DistrictMonitorStatistic> getStatisticDtoByTaskIds(Set<Integer> screeningTaskIds) {
        LambdaQueryWrapper<DistrictMonitorStatistic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DistrictMonitorStatistic::getScreeningTaskId, screeningTaskIds);
        List<DistrictMonitorStatistic> districtMonitorStatistics = baseMapper.selectList(queryWrapper);
        return districtMonitorStatistics;
    }

    /**
     * 获取数据
     *
     * @param noticeId
     * @param districtId
     * @param user
     * @return
     * @throws IOException
     */
    public List<DistrictMonitorStatistic> getStatisticDtoByNoticeIdAndUser(Integer noticeId, Integer currentDistrictId, CurrentUser user, boolean istotal, boolean isCurrent) throws IOException {
        if (noticeId == null || user == null) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<DistrictMonitorStatistic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DistrictMonitorStatistic::getScreeningNoticeId, noticeId);
        queryWrapper.eq(DistrictMonitorStatistic::getIsTotal, istotal);
        if (isCurrent) {
            queryWrapper.eq(DistrictMonitorStatistic::getDistrictId,currentDistrictId);
        } else {
            Set<Integer> districtIds = districtService.getChildDistrictIdsByDistrictId(currentDistrictId);
            districtIds.add(currentDistrictId);
            queryWrapper.in(DistrictMonitorStatistic::getDistrictId, districtIds);
        }
        List<DistrictMonitorStatistic> districtMonitorStatistics = baseMapper.selectList(queryWrapper);
        return districtMonitorStatistics;
    }

    /**
     * 根据唯一索引批量新增或更新
     * @param districtMonitorStatistics
     */
    public void batchSaveOrUpdate(List<DistrictMonitorStatistic> districtMonitorStatistics) {
        if (CollectionUtils.isEmpty(districtMonitorStatistics)) {
            return;
        }
        baseMapper.batchSaveOrUpdate(districtMonitorStatistics);
    }
}
