package com.wupol.myopia.business.core.stat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.stat.domain.mapper.DistrictVisionStatisticMapper;
import com.wupol.myopia.business.core.stat.domain.model.DistrictVisionStatistic;
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
public class DistrictVisionStatisticService extends BaseService<DistrictVisionStatisticMapper, DistrictVisionStatistic> {

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
            districtVisionStatistics.addAll(this.list(queryWrapper));
        });
        return districtVisionStatistics;
    }

    /**
     * 根据条件查找所有数据
     *
     * @param noticeId
     * @param currentDistrictId
     * @param user
     * @param istotal
     * @return
     * @throws IOException
     */
    public List<DistrictVisionStatistic> getStatisticDtoByNoticeIdAndCurrentDistrictId(Integer noticeId, Integer currentDistrictId, CurrentUser user, boolean istotal) throws IOException {
        if (noticeId == null || user == null) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<DistrictVisionStatistic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DistrictVisionStatistic::getScreeningNoticeId, noticeId);
        queryWrapper.eq(DistrictVisionStatistic::getIsTotal, istotal);
        queryWrapper.eq(DistrictVisionStatistic::getDistrictId, currentDistrictId);
        return baseMapper.selectList(queryWrapper);
    }

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
