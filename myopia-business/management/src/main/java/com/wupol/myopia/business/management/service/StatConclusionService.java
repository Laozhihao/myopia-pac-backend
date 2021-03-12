package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.exceptions.ManagementUncheckedException;
import com.wupol.myopia.business.management.domain.builder.StatConclusionBuilder;
import com.wupol.myopia.business.management.domain.mapper.StatConclusionMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.management.domain.model.StatConclusion;
import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.management.domain.query.StatConclusionQuery;
import com.wupol.myopia.business.management.util.TwoTuple;

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
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

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
     *
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
    /**
     * 保存并更新
     *
     * @param visionScreeningResult
     */
    public void saveOrUpdateStudentScreenData(TwoTuple<VisionScreeningResult, VisionScreeningResult> allFirstAndSecondResult) {
        StatConclusion statConclusion = getScreeningConclusionResult(allFirstAndSecondResult);
        if (statConclusion.getId() != null) {
            //更新
            updateById(statConclusion);
        } else {
            //创建
            save(statConclusion);
        }
    }

    /**
     * 获取统计数据
     *
     * @param visionScreeningResult
     * @return
     */
    private StatConclusion getScreeningConclusionResult(TwoTuple<VisionScreeningResult, VisionScreeningResult> allFirstAndSecondResult) {
        VisionScreeningResult currentVisionScreeningResult = allFirstAndSecondResult.getFirst();
        VisionScreeningResult secondVisionScreeningResult = allFirstAndSecondResult.getSecond();
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.getById(currentVisionScreeningResult.getScreeningPlanSchoolStudentId());
        if (screeningPlanSchoolStudent == null) {
            throw new ManagementUncheckedException("数据异常，无法根据id找到对应的ScreeningPlanSchoolStudent对象，id = " + currentVisionScreeningResult.getScreeningPlanSchoolStudentId());
        }
        // 根据是否复查，查找结论表
        StatConclusion statConclusion = this.getStatConclusion(currentVisionScreeningResult.getId(), currentVisionScreeningResult.getIsDoubleScreen());
        //  尝试查找另外一半的数据
        //需要新增
        StatConclusionBuilder statConclusionBuilder = StatConclusionBuilder.getStatConclusionBuilder();
        statConclusion = statConclusionBuilder.setCurrentVisionScreeningResult(currentVisionScreeningResult,secondVisionScreeningResult).setStatConclusion(statConclusion)
                .setScreeningPlanSchoolStudent(screeningPlanSchoolStudent)
                .build();
        return statConclusion;
    }

    private StatConclusion getStatConclusion(Integer resultId, Boolean isDoubleScreen) {
        LambdaQueryWrapper<StatConclusion> queryWrapper = new LambdaQueryWrapper<>();
        StatConclusion statConclusion = new StatConclusion().setResultId(resultId);
        statConclusion.setIsRescreen(isDoubleScreen);
        queryWrapper.setEntity(statConclusion);
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 获取统计数据
     *
     * @param visionScreeningResult
     * @return
     */
    private StatConclusion getScreeningConclusionResult(
            VisionScreeningResult visionScreeningResult) {
        if (visionScreeningResult == null) {
        }
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent =
                screeningPlanSchoolStudentService.getById(
                        visionScreeningResult.getScreeningPlanSchoolStudentId());
        if (screeningPlanSchoolStudent == null) {
            throw new ManagementUncheckedException(
                    "数据异常，无法根据id找到对应的ScreeningPlanSchoolStudent对象，id = "
                            + visionScreeningResult.getScreeningPlanSchoolStudentId());
        }
        // 根据是否复查，查找结论表
        LambdaQueryWrapper<StatConclusion> queryWrapper = new LambdaQueryWrapper<>();
        StatConclusion statConclusion =
                new StatConclusion().setResultId(visionScreeningResult.getId());
        statConclusion.setIsRescreen(visionScreeningResult.getIsDoubleScreen());
        queryWrapper.setEntity(statConclusion);
        statConclusion = baseMapper.selectOne(queryWrapper);

        //需要新增
        StatConclusionBuilder statConclusionBuilder =
                StatConclusionBuilder.getStatConclusionBuilder();
        statConclusion = statConclusionBuilder.setVisionScreeningResult(visionScreeningResult)
                .setStatConclusion(statConclusion)
                .setScreeningPlanSchoolStudent(screeningPlanSchoolStudent)
                .build();
        return statConclusion;
    }
}





}
