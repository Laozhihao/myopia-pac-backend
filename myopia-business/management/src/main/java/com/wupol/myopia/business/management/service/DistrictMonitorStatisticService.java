package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.DistrictMonitorStatisticMapper;
import com.wupol.myopia.business.management.domain.model.DistrictMonitorStatistic;
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
     * @param noticeId
     * @param districtId
     * @param user
     * @return
     * @throws IOException
     */
    public List<DistrictMonitorStatistic> getStatisticDtoByNoticeIdAndUser(Integer noticeId, Integer districtId, CurrentUser user) throws IOException {
        if (noticeId == null || user == null) {
            return new ArrayList<>();
        }
        if (user.isScreeningUser()) {
            throw new BusinessException("筛查人员没有权限访问该数据");
        }
        LambdaQueryWrapper<DistrictMonitorStatistic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DistrictMonitorStatistic::getScreeningNoticeId, noticeId);
        if (user.isGovDeptUser()) {
            Set<Integer> districtIds = districtService.getChildDistrictIdsByDistrictId(districtId);
            districtIds.add(districtId);
            queryWrapper.in(DistrictMonitorStatistic::getDistrictId, districtIds);
        }
        List<DistrictMonitorStatistic> districtMonitorStatistics = baseMapper.selectList(queryWrapper);
        return districtMonitorStatistics;
    }
}
