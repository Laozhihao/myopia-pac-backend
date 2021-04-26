package com.wupol.myopia.business.api.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.core.government.service.DistrictService;
import com.wupol.myopia.business.core.stat.domain.model.DistrictMonitorStatistic;
import com.wupol.myopia.business.core.stat.service.DistrictMonitorStatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @Author wulizhou
 * @Date 2021/4/26 17:23
 */
@Service
public class DistrictMonitorStatisticBizService {

    @Autowired
    private DistrictMonitorStatisticService districtMonitorStatisticService;

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
            districtMonitorStatistics.addAll(districtMonitorStatisticService.list(queryWrapper));
        });
        return districtMonitorStatistics;
    }

}
