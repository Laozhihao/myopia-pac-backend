package com.wupol.myopia.business.core.screening.flow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.*;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.StatConclusionMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author Jacob
 * @Date 2021-02-22
 */
@Service
public class StatConclusionService extends BaseService<StatConclusionMapper, StatConclusion> {

    /**
     * 获取筛查结论列表
     *
     * @param statConclusionQueryDTO
     * @return
     */
    public List<StatConclusion> listByQuery(StatConclusionQueryDTO statConclusionQueryDTO) {
        return baseMapper.listByQuery(statConclusionQueryDTO);
    }

    /**
     * 获取最后一个
     * @param statConclusionQueryDTO
     * @return
     */
    public StatConclusion getLastOne(StatConclusionQueryDTO statConclusionQueryDTO) {
        return baseMapper.selectLastOne(statConclusionQueryDTO);
    }

    /**
     * 根据源通知ID获取筛查数据
     *
     * @param screeningNoticeId
     * @return
     */
    public List<StatConclusion> getBySrcScreeningNoticeId(Integer screeningNoticeId) {
        LambdaQueryWrapper<StatConclusion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatConclusion::getSrcScreeningNoticeId, screeningNoticeId);
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 根据筛查计划获取筛查结论Vo列表
     * @param screeningPlanId
     * @return
     */
    public List<StatConclusionDTO> getVoByScreeningPlanId(Integer screeningPlanId) {
        return baseMapper.selectVoByScreeningPlanId(screeningPlanId);
    }


    public StatConclusion getStatConclusion(Integer resultId, Boolean isDoubleScreen) {
        LambdaQueryWrapper<StatConclusion> queryWrapper = new LambdaQueryWrapper<>();
        StatConclusion statConclusion = new StatConclusion().setResultId(resultId);
        statConclusion.setIsRescreen(isDoubleScreen);
        queryWrapper.setEntity(statConclusion);
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 根据筛查通知ID与区域Id列表查出导出的筛查数据
     * @param screeningNoticeId
     * @param districtIds
     * @return
     */
    public List<StatConclusionExportDTO> getExportVoByScreeningNoticeIdAndDistrictIds(Integer screeningNoticeId, List<Integer> districtIds) {
        if (CollectionUtils.isEmpty(districtIds)) {
            return Collections.emptyList();
        }
        return baseMapper.selectExportVoByScreeningNoticeIdAndDistrictIds(screeningNoticeId, districtIds);
    }

    /**
     * 根据筛查通知ID与学校Id查出导出的筛查数据
     * @param screeningNoticeId
     * @param schoolId
     * @return
     */
    public List<StatConclusionExportDTO> getExportVoByScreeningNoticeIdAndSchoolId(Integer screeningNoticeId, Integer schoolId) {
        return baseMapper.selectExportVoByScreeningNoticeIdAndSchoolId(screeningNoticeId, schoolId);
    }

    /**
     * 根据筛查计划ID与学校Id查出导出的筛查数据
     * @param screeningPlanId
     * @param schoolId
     * @return
     */
    public List<StatConclusionExportDTO> getExportVoByScreeningPlanIdAndSchoolId(Integer screeningPlanId, Integer schoolId) {
        return baseMapper.selectExportVoByScreeningPlanIdAndSchoolId(screeningPlanId, schoolId);
    }

    /**
     * 根据筛查通知ID与学校Id查出报告的筛查数据
     * @param screeningNoticeId
     * @param schoolId
     * @return
     */
    public List<StatConclusionReportDTO> getReportVo(
            Integer screeningNoticeId, Integer planId, Integer schoolId) {
        return baseMapper.selectReportVoByQuery(screeningNoticeId, planId, schoolId);
    }

    /**
     * 根据筛查通知ID与筛查机构Id查出导出的筛查数据
     * @param screeningNoticeId
     * @param screeningOrgId
     * @return
     */
    public List<StatConclusionExportDTO> getExportVoByScreeningNoticeIdAndScreeningOrgId(Integer screeningNoticeId, Integer screeningOrgId) {
        return baseMapper.selectExportVoByScreeningNoticeIdAndScreeningOrgId(screeningNoticeId, screeningOrgId);
    }

    /**
     * 根据筛查计划ID与筛查机构Id查出导出的筛查数据
     * @param screeningPlanId
     * @param screeningOrgId
     * @return
     */
    public List<StatConclusionExportDTO> getExportVoByScreeningPlanIdAndScreeningOrgId(Integer screeningPlanId, Integer screeningOrgId) {
        return baseMapper.selectExportVoByScreeningPlanIdAndScreeningOrgId(screeningPlanId, screeningOrgId);
    }

    /**
     * 根据筛查通知ID获取学校ID
     *
     * @param noticeId 筛查通知ID
     * @return java.util.List<java.lang.Integer>
     **/
    public List<Integer> getSchoolIdsByScreeningNoticeIdAndDistrictIds(Integer noticeId, List<Integer> districtIds) {
        Assert.notNull(noticeId, "筛查通知ID不能为空");
        return baseMapper.selectSchoolIdsByScreeningNoticeIdAndDistrictIds(noticeId, districtIds);
    }

    /**
     * 根据筛查计划ID获取学校ID
     *
     * @param planId 筛查计划ID
     * @return java.util.List<java.lang.Integer>
     **/
    public List<Integer> getSchoolIdByPlanId(Integer planId) {
        Assert.notNull(planId, "筛查计划ID不能为空");
        return baseMapper.selectSchoolIdByPlanId(planId);
    }

    /**
     * 获取指定时间内进行复测的计划及学校
     * @param date
     * @return
     */
    public List<ScreenPlanSchoolDTO> getRescreenPlanSchoolByTime(Date date) {
        return baseMapper.getPlanSchoolByDate(date, true);
    }

    /**
     * 根据学生id获取所有筛查结果
     *
     * @param studentIdList
     */
    public Set<Integer> getHasNormalVisionStudentIds(Set<Integer> studentIdList) {
        if (CollectionUtils.isEmpty(studentIdList)) {
            return Collections.emptySet();
        }
        LambdaQueryWrapper<StatConclusion> statConclusionLambdaQueryWrapper = new LambdaQueryWrapper<>();
        statConclusionLambdaQueryWrapper.in(StatConclusion::getStudentId, studentIdList);
        List<StatConclusion> statConclusions = list(statConclusionLambdaQueryWrapper);
        if (CollectionUtils.isEmpty(statConclusions)) {
            return Collections.emptySet();
        }
        // 根据studentId进行分组

        Map<Integer, Boolean> studentVisionExceptionMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getStudentId,
                Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparing(StatConclusion::getVisionWarningUpdateTime)),
                        statConclusionOptional -> {
                            if (statConclusionOptional.isPresent()) {
                                return statConclusionOptional.get().getIsVisionWarning();
                            } else {
                                return false;
                            }
                        })));

       return studentVisionExceptionMap.keySet().stream().filter(studentId -> {
            Boolean isVisionException = studentVisionExceptionMap.get(studentId);
            return isVisionException == null || !isVisionException;
        }).collect(Collectors.toSet());

    }

    /**
     * 获取门口个筛查时间范围的统计数据
     * @return
     */
    public List<StatConclusion> getStatConclusionByDateTimeRange(Date yesterdayDateTime,Date todayDateTime) {
        LambdaQueryWrapper<StatConclusion> statConclusionLambdaQueryWrapper = new LambdaQueryWrapper<>();
        statConclusionLambdaQueryWrapper.select(StatConclusion::getStudentId, StatConclusion::getIsVisionWarning, StatConclusion::getVisionWarningUpdateTime)
                .gt(StatConclusion::getVisionWarningUpdateTime, yesterdayDateTime).le(StatConclusion::getVisionWarningUpdateTime, todayDateTime);
        return list(statConclusionLambdaQueryWrapper);
    }
}

