package com.wupol.myopia.business.core.screening.flow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.exceptions.ManagementUncheckedException;
import com.wupol.myopia.business.management.domain.builder.StatConclusionBuilder;
import com.wupol.myopia.business.management.domain.dto.BigScreenStatDataDTO;
import com.wupol.myopia.business.management.domain.mapper.StatConclusionMapper;
import com.wupol.myopia.business.management.domain.model.SchoolGrade;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.management.domain.model.StatConclusion;
import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.management.domain.query.StatConclusionQuery;
import com.wupol.myopia.business.management.domain.vo.StatConclusionExportVo;
import com.wupol.myopia.business.management.domain.vo.StatConclusionReportVo;
import com.wupol.myopia.business.management.domain.vo.StatConclusionDTO;
import com.wupol.myopia.business.management.util.TwoTuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    @Autowired
    private SchoolGradeService schoolGradeService;

    /**
     * 获取筛查结论列表
     *
     * @param statConclusionQuery
     * @return
     */
    public List<StatConclusion> listByQuery(StatConclusionQuery statConclusionQuery) {
        return statConclusionMapper.listByQuery(statConclusionQuery);
    }

    public StatConclusion getLastOne(StatConclusionQuery statConclusionQuery) {
        return statConclusionMapper.selectLastOne(statConclusionQuery);
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
        return statConclusionMapper.selectList(queryWrapper);
    }

    /**
     * 根据筛查计划获取筛查结论Vo列表
     * @param screeningPlanId
     * @return
     */
    public List<StatConclusionDTO> getVoByScreeningPlanId(Integer screeningPlanId) {
        return statConclusionMapper.selectVoByScreeningPlanId(screeningPlanId);
    }
    /**
     * 保存并更新
     *
     * @param allFirstAndSecondResult
     */
    public StatConclusion saveOrUpdateStudentScreenData(TwoTuple<VisionScreeningResult, VisionScreeningResult> allFirstAndSecondResult) {
        StatConclusion statConclusion = getScreeningConclusionResult(allFirstAndSecondResult);
        if (statConclusion.getId() != null) {
            //更新
            updateById(statConclusion);
        } else {
            //创建
            save(statConclusion);
        }
        return statConclusion;
    }

    /**
     * 获取统计数据
     *
     * @param allFirstAndSecondResult
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
        //需要新增
        SchoolGrade schoolGrade = schoolGradeService.getById(screeningPlanSchoolStudent.getGradeId());
        StatConclusionBuilder statConclusionBuilder = StatConclusionBuilder.getStatConclusionBuilder();
        statConclusion = statConclusionBuilder.setCurrentVisionScreeningResult(currentVisionScreeningResult,secondVisionScreeningResult).setStatConclusion(statConclusion)
                .setScreeningPlanSchoolStudent(screeningPlanSchoolStudent).setGradeCode(schoolGrade.getGradeCode())
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
     * 根据筛查通知ID与区域Id列表查出导出的筛查数据
     * @param screeningNoticeId
     * @param districtIds
     * @return
     */
    public List<StatConclusionExportVo> getExportVoByScreeningNoticeIdAndDistrictIds(Integer screeningNoticeId, List<Integer> districtIds) {
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
    public List<StatConclusionExportVo> getExportVoByScreeningNoticeIdAndSchoolId(Integer screeningNoticeId, Integer schoolId) {
        return baseMapper.selectExportVoByScreeningNoticeIdAndSchoolId(screeningNoticeId, schoolId);
    }

    /**
     * 根据筛查计划ID与学校Id查出导出的筛查数据
     * @param screeningPlanId
     * @param schoolId
     * @return
     */
    public List<StatConclusionExportVo> getExportVoByScreeningPlanIdAndSchoolId(Integer screeningPlanId, Integer schoolId) {
        return baseMapper.selectExportVoByScreeningPlanIdAndSchoolId(screeningPlanId, schoolId);
    }

    /**
     * 根据筛查通知ID与学校Id查出报告的筛查数据
     * @param screeningNoticeId
     * @param schoolId
     * @return
     */
    public List<StatConclusionReportVo> getReportVo(
            Integer screeningNoticeId, Integer planId, Integer schoolId) {
        return baseMapper.selectReportVoByQuery(screeningNoticeId, planId, schoolId);
    }

    /**
     * 获取通知
     * @param cityDistrictIdList
     * @param noticeId
     * @return
     */
    public List<BigScreenStatDataDTO> getByNoticeidAndDistrictIds(Integer noticeId) {
        LambdaQueryWrapper<StatConclusion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatConclusion::getSrcScreeningNoticeId, noticeId);
        queryWrapper.eq(StatConclusion::getIsRescreen,false);
        List<StatConclusion> statConclusionList = baseMapper.selectList(queryWrapper);
        List<BigScreenStatDataDTO> bigScreenStatDataDTOs = this.getBigScreenStatDataDTOList(statConclusionList);
        return  bigScreenStatDataDTOs;
    }

    /**
     * 获取大屏统计的基础数据
     * @param statConclusionList
     * @return
     */
    private List<BigScreenStatDataDTO> getBigScreenStatDataDTOList(List<StatConclusion> statConclusionList) {
      return   statConclusionList.stream().map(statConclusion ->    BigScreenStatDataDTO.getInstance(statConclusion)).collect(Collectors.toList());
    }

    /**
     * 根据筛查通知ID与筛查机构Id查出导出的筛查数据
     * @param screeningNoticeId
     * @param screeningOrgId
     * @return
     */
    public List<StatConclusionExportVo> getExportVoByScreeningNoticeIdAndScreeningOrgId(Integer screeningNoticeId, Integer screeningOrgId) {
        return baseMapper.selectExportVoByScreeningNoticeIdAndScreeningOrgId(screeningNoticeId, screeningOrgId);
    }

    /**
     * 根据筛查计划ID与筛查机构Id查出导出的筛查数据
     * @param screeningPlanId
     * @param screeningOrgId
     * @return
     */
    public List<StatConclusionExportVo> getExportVoByScreeningPlanIdAndScreeningOrgId(Integer screeningPlanId, Integer screeningOrgId) {
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
}

