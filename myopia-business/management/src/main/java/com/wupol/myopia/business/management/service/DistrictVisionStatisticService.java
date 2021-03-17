package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.framework.core.util.CollectionUtils;
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
    public List<DistrictVisionStatistic> getStatisticDtoByNoticeIdAndUser(Integer noticeId, Integer districtId, CurrentUser user) throws IOException {
        if (noticeId == null || user == null) {
            return new ArrayList<>();
        }
        if (user.isScreeningUser()) {
            throw new BusinessException("筛查人员没有权限访问该数据");
        }
        LambdaQueryWrapper<DistrictVisionStatistic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DistrictVisionStatistic::getScreeningNoticeId, noticeId);
        Set<Integer> districtIds = districtService.getChildDistrictIdsByDistrictId(districtId);
        districtIds.add(districtId);
        queryWrapper.in(DistrictVisionStatistic::getDistrictId, districtIds);
        List<DistrictVisionStatistic> districtVisionStatistics = baseMapper.selectList(queryWrapper);
        return districtVisionStatistics;
    }

    /**
     * 根据唯一索引批量新增或更新
     * @param districtVisionStatistics
     */
    public void batchSaveOrUpdate(List<DistrictVisionStatistic> districtVisionStatistics) {
        if (CollectionUtils.isEmpty(districtVisionStatistics)) {
            return;
        }
        baseMapper.batchSaveOrUpdate(districtVisionStatistics);
    }
}
