package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.StatConclusionMapper;
import com.wupol.myopia.business.management.domain.model.StatConclusion;
import com.wupol.myopia.business.management.domain.query.StatConclusionQuery;

import com.wupol.myopia.business.management.domain.vo.StatConclusionVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author Jacob
 * @Date 2021-02-22
 */
@Service
public class StatConclusionService extends BaseService<StatConclusionMapper, StatConclusion> {
    @Autowired
    private StatConclusionMapper statConclusionMapper;

    /**
     * 获取筛查结论列表
     *
     * @param statConclusionQuery
     * @return
     */
    public List<StatConclusion> listByQuery(StatConclusionQuery statConclusionQuery) {
        return statConclusionMapper.listByQuery(statConclusionQuery);
    }

    /**
     * 根据源通知ID获取处理后有效的筛查数据
     *
     * @param screeningNoticeId
     * @return
     */
    public List<StatConclusion> getBySrcScreeningNoticeId(Integer screeningNoticeId) {
        LambdaQueryWrapper<StatConclusion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatConclusion::getSrcScreeningNoticeId, screeningNoticeId)
                .eq(StatConclusion::getIsValid, true);
        return statConclusionMapper.selectList(queryWrapper);
    }

    public StatConclusion getLastOne(StatConclusionQuery statConclusionQuery) {
        return statConclusionMapper.selectLastOne(statConclusionQuery);
    }

    /**
     * 根据源通知ID获取处理后有效的筛查数据
     * @param screeningNoticeId
     * @return
     */
    public List<StatConclusion> getValidBySrcScreeningNoticeId(Integer screeningNoticeId) {
        LambdaQueryWrapper<StatConclusion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatConclusion::getSrcScreeningNoticeId, screeningNoticeId)
                .eq(StatConclusion::getIsValid, true);
        return statConclusionMapper.selectList(queryWrapper);
    }

    /**
     * 根据筛查计划获取筛查结论Vo列表
     * @param screeningPlanId
     * @return
     */
    public List<StatConclusionVo> getValidVoByScreeningPlanId(Integer screeningPlanId) {
        return statConclusionMapper.selectValidVoByScreeningPlanId(screeningPlanId);
    }
}
