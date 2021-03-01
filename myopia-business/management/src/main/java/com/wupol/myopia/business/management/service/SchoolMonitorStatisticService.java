package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.SchoolMonitorStatisticMapper;
import com.wupol.myopia.business.management.domain.model.SchoolMonitorStatistic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @Author HaoHao
 * @Date 2021-02-24
 */
@Service
public class SchoolMonitorStatisticService extends BaseService<SchoolMonitorStatisticMapper, SchoolMonitorStatistic> {

    @Autowired
    private DistrictService districtService;

    public List<SchoolMonitorStatistic> getStatisticDtoByNoticeIdAndOrgId(Integer noticeId, CurrentUser user,Integer districtId) throws IOException {
        if (noticeId == null || user == null) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<SchoolMonitorStatistic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SchoolMonitorStatistic::getScreeningNoticeId, noticeId);
        if (user.isScreeningUser()) {
            queryWrapper.eq(SchoolMonitorStatistic::getScreeningOrgId,user.getOrgId());
        } else if (user.isGovDeptUser()) {
            Set<Integer> childDistrictIds  = districtService.getChildDistrictIdsByDistrictId(districtId);
            queryWrapper.in(SchoolMonitorStatistic::getDistrictId,childDistrictIds);
        }
        List<SchoolMonitorStatistic> SchoolMonitorStatistics = baseMapper.selectList(queryWrapper);
        return SchoolMonitorStatistics;
    }
}
