package com.wupol.myopia.business.core.stat.service;

import com.google.common.collect.Lists;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.stat.domain.mapper.DistrictVisionStatisticMapper;
import com.wupol.myopia.business.core.stat.domain.model.DistrictVisionStatistic;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Service
public class DistrictVisionStatisticService extends BaseService<DistrictVisionStatisticMapper, DistrictVisionStatistic> {


    /**
     * 根据唯一索引批量新增或更新
     * @param districtVisionStatistics
     */
    public void batchSaveOrUpdate(List<DistrictVisionStatistic> districtVisionStatistics) {
        if (CollectionUtils.isEmpty(districtVisionStatistics)) {
            return;
        }
        Lists.partition(districtVisionStatistics, 20).forEach(statistics -> baseMapper.batchSaveOrUpdate(statistics));
    }
}
