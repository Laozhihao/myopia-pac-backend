package com.wupol.myopia.business.management.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.StatConclusionMapper;
import com.wupol.myopia.business.management.domain.model.StatConclusion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author Jacob
 * @Date 2021-02-22
 */
@Service
public class StatConclusionService extends BaseService<StatConclusionMapper, StatConclusion> {
    @Autowired
    private StatConclusionMapper statConclusionMapper;

    public StatConclusion getLastOne(List<Integer> districtIds) {
        return statConclusionMapper.selectLastOne(districtIds);
    }

    public List<StatConclusion> listByDateRange(
            List<Integer> districtIds, Date startDate, Date endDate) {
        return statConclusionMapper.listByDateRange(districtIds, startDate, endDate);
    }

    public List<StatConclusion> listByNoticeId(int noticeId, List<Integer> districtIds) {
        return statConclusionMapper.listByNoticeId(noticeId, districtIds);
    }
}
