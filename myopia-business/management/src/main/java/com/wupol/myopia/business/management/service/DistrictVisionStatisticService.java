package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.DistrictVisionStatisticMapper;
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
public class DistrictVisionStatisticService extends BaseService<DistrictVisionStatisticMapper, DistrictVisionStatistic> {

    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private DistrictService districtService;


    public List<DistrictVisionStatistic> getStatisticDtoByNoticeIds(Set<Integer> screeningTaskIds) {
        LambdaQueryWrapper<DistrictVisionStatistic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DistrictVisionStatistic::getScreeningTaskId, screeningTaskIds);
        List<DistrictVisionStatistic> DistrictVisionStatistics = baseMapper.selectList(queryWrapper);
        return DistrictVisionStatistics;
    }

    /**
     * 根据条件查找所有数据
     *
     * @param noticeId
     * @param districtId
     * @param user
     * @return
     * @throws IOException
     */
    public List<DistrictVisionStatistic> getStatisticDtoByNoticeIdAndUser(Integer noticeId, Integer currentDistrictId, CurrentUser user, boolean istotal, boolean isCurrent) throws IOException {
        if (noticeId == null || user == null) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<DistrictVisionStatistic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DistrictVisionStatistic::getScreeningNoticeId, noticeId);
        queryWrapper.eq(DistrictVisionStatistic::getIsTotal, istotal);
        if (isCurrent) {
            queryWrapper.eq(DistrictVisionStatistic::getDistrictId, currentDistrictId);
        } else {
            Set<Integer> districtIds = districtService.getChildDistrictIdsByDistrictId(currentDistrictId);
            districtIds.add(currentDistrictId);
            queryWrapper.in(DistrictVisionStatistic::getDistrictId, districtIds);
        }
        List<DistrictVisionStatistic> districtVisionStatistics = baseMapper.selectList(queryWrapper);
        return districtVisionStatistics;
    }
}
