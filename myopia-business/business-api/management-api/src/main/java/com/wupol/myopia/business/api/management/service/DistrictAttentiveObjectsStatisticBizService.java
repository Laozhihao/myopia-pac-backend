package com.wupol.myopia.business.api.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.core.stat.domain.model.DistrictAttentiveObjectsStatistic;
import com.wupol.myopia.business.core.stat.service.DistrictAttentiveObjectsStatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author wulizhou
 * @Date 2021/4/26 16:55
 */
@Service
public class DistrictAttentiveObjectsStatisticBizService {

    @Autowired
    private DistrictAttentiveObjectsStatisticService districtAttentiveObjectsStatisticService;

    @Autowired
    private DistrictBizService districtBizService;

    /**
     * 通过用户获取user
     *
     * @param user
     * @return
     */
    public List<DistrictAttentiveObjectsStatistic> getDataByUser(CurrentUser user) throws IOException {
        List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics = new ArrayList<>();
        if (user.isPlatformAdminUser()) {
            return districtAttentiveObjectsStatisticService.list(new LambdaQueryWrapper<>());
        }
        // 查找所有数据
        Lists.partition(districtBizService.getCurrentUserDistrictTreeAllIds(user), 100).forEach(districtIds -> {
            // 调整为根据districtId获取
            LambdaQueryWrapper<DistrictAttentiveObjectsStatistic> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(DistrictAttentiveObjectsStatistic::getDistrictId, districtIds);
            districtAttentiveObjectsStatistics.addAll(districtAttentiveObjectsStatisticService.list(queryWrapper));
        });
        return districtAttentiveObjectsStatistics;
    }

}
