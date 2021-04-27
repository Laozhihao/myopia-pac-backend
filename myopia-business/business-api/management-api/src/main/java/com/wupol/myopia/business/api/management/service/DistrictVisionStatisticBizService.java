package com.wupol.myopia.business.api.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.core.government.service.DistrictService;
import com.wupol.myopia.business.core.stat.domain.model.DistrictVisionStatistic;
import com.wupol.myopia.business.core.stat.service.DistrictVisionStatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @Author wulizhou
 * @Date 2021/4/26 17:17
 */
@Service
public class DistrictVisionStatisticBizService {

    @Autowired
    private DistrictService districtService;

    @Autowired
    private DistrictVisionStatisticService districtVisionStatisticService;

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
    public List<DistrictVisionStatistic> getStatisticDtoByNoticeIdAndCurrentChildDistrictIds(Integer noticeId, Integer currentDistrictId, CurrentUser user, boolean istotal) throws IOException {
        if (noticeId == null || user == null) {
            return new ArrayList<>();
        }
        List<DistrictVisionStatistic> districtVisionStatistics = new ArrayList<>();
        Set<Integer> districtIds = districtService.getChildDistrictIdsByDistrictId(currentDistrictId);
        districtIds.add(currentDistrictId);
        Lists.partition(new ArrayList<>(districtIds), 100).forEach(districtIdList -> {
            LambdaQueryWrapper<DistrictVisionStatistic> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DistrictVisionStatistic::getScreeningNoticeId, noticeId);
            queryWrapper.eq(DistrictVisionStatistic::getIsTotal, istotal);
            queryWrapper.in(DistrictVisionStatistic::getDistrictId, districtIdList);
            districtVisionStatistics.addAll(districtVisionStatisticService.list(queryWrapper));
        });
        return districtVisionStatistics;
    }

}
