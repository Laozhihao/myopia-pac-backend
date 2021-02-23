package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

    /**
     * 根据源通知ID获取处理后有效的筛查数据
     * @param screeningNoticeId
     * @return
     */
    public List<StatConclusion> getBySrcScreeningNoticeId(Integer screeningNoticeId) {
        LambdaQueryWrapper<StatConclusion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatConclusion::getSrcScreeningNoticeId, screeningNoticeId).eq(StatConclusion::getIsValid, StatConclusion.YES);
        return statConclusionMapper.selectList(queryWrapper);
    }
}
