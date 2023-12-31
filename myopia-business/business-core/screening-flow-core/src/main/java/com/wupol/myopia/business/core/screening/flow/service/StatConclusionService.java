package com.wupol.myopia.business.core.screening.flow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.screening.flow.domain.dos.SchoolCountDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.WarningLevelCountDO;
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
     * 保存并更新
     *
     * @param statConclusion
     */
    public StatConclusion saveOrUpdateStudentScreenData(StatConclusion statConclusion) {
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
     * 获取筛查结论列表
     *
     * @param statConclusionQueryDTO
     * @return
     */
    public List<StatConclusion> listByQuery(StatConclusionQueryDTO statConclusionQueryDTO) {
        return baseMapper.listByQuery(statConclusionQueryDTO);
    }

    /**
     * 获取list（已发布的计划的）
     *
     * @param statConclusionQueryDTO 查询条件
     * @return java.util.List<com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion>
     **/
    public List<StatConclusion> listOfReleasePlanByQuery(StatConclusionQueryDTO statConclusionQueryDTO) {
        return baseMapper.listOfReleasePlanByQuery(statConclusionQueryDTO);
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
     * 根据源通知ID集获取筛查数据
     *
     * @param screeningNoticeIds
     * @param excludePlanIds
     * @return
     */
    public List<StatConclusion> listOfReleasePlanByScreeningNoticeIds(List<Integer> screeningNoticeIds, List<Integer> excludePlanIds) {
        return listOfReleasePlanByQuery(new StatConclusionQueryDTO().setScreeningNoticeIds(screeningNoticeIds).setExcludePlanIds(excludePlanIds));
    }

    /**
     * 根据筛查计划获取筛查结论Vo列表
     * @param screeningPlanId
     * @return
     */
    public List<StatConclusion> getVoByScreeningPlanId(Integer screeningPlanId) {
        LambdaQueryWrapper<StatConclusion> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(StatConclusion::getPlanId,screeningPlanId);
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 批量根据筛查计划ID获取筛查结论
     * @param screeningPlanIds 筛查计划ID集合
     */
    public List<StatConclusion> getByScreeningPlanIds(List<Integer> screeningPlanIds) {
        return baseMapper.selectList(Wrappers.lambdaQuery(StatConclusion.class)
                .in(StatConclusion::getPlanId,screeningPlanIds));
    }

    /**
     * 根据筛查计划ID和学校ID获取筛查结论
     * @param screeningPlanId 筛查计划ID
     * @param schoolId 学校ID
     */
    public List<StatConclusion> listByScreeningPlanIdAndSchoolId(Integer screeningPlanId,Integer schoolId) {
        return baseMapper.selectList(Wrappers.lambdaQuery(StatConclusion.class)
                .eq(StatConclusion::getPlanId,screeningPlanId)
                .eq(StatConclusion::getSchoolId,schoolId));
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
    public List<StatConclusionExportDTO> getExportVoByScreeningNoticeIdAndDistrictIds(Integer screeningNoticeId, List<Integer> districtIds,Boolean isKindergarten) {
        if (CollectionUtils.isEmpty(districtIds)) {
            return Collections.emptyList();
        }
        return baseMapper.selectExportVoByScreeningNoticeIdAndDistrictIds(screeningNoticeId, districtIds,isKindergarten);
    }

    /**
     * 根据筛查通知ID与区域Id列表查出导出的筛查数据并且分组
     * @param screeningNoticeId
     * @param districtIds
     * @return
     */
    public List<ExportPlanSchool> getPlanSchoolGradeClassHasData(Integer screeningNoticeId, List<Integer> districtIds) {
        if (CollectionUtils.isEmpty(districtIds)) {
            return Collections.emptyList();
        }
        return baseMapper.selectPlanSchoolGradeClassHasData(screeningNoticeId, districtIds);
    }

    /**
     * 根据筛查计划ID与学校Id查出导出的筛查数据
     * @param screeningPlanId
     * @param schoolId
     * @return
     */
    public List<StatConclusionExportDTO> getExportVoByScreeningPlanIdAndSchoolId(Integer screeningPlanId, Integer schoolId,Boolean isRescreen) {
        return baseMapper.selectExportVoByScreeningPlanIdAndSchoolId(screeningPlanId, schoolId,isRescreen);
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
     * @Description:
     * @Param: [screeningPlanId, screeningOrgId, 学校ID, 年级名称, 班级名称]
     * @return: java.util.List<com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionExportDTO>
     * @Author: 钓猫的小鱼
     * @Date: 2021/12/31
     */
    public List<StatConclusionExportDTO> selectExportVoBySPlanIdAndSOrgIdAndSChoolIdAndGradeNameAndClassanme(Integer screeningPlanId, Integer screeningOrgId, Integer schoolId, Integer gradeId, Integer classId,Boolean isKindergarten) {
        return baseMapper.selectExportVoBySPlanIdAndSOrgIdAndSChoolIdAndGradeNameAndClassanme(screeningPlanId,screeningOrgId,schoolId,gradeId,classId,isKindergarten);
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
       return studentVisionExceptionMap.keySet().stream().filter(studentId -> !Boolean.TRUE.equals(studentVisionExceptionMap.get(studentId))).collect(Collectors.toSet());
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

    /**
     * 获取下一条筛查统计
     *
     * @param statConclusionId 表ID
     * @param studentId 学校ID
     * @return com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion
     **/
    public StatConclusion getNextScreeningStat(Integer statConclusionId, Integer studentId) {
        return baseMapper.getNextScreeningStat(statConclusionId, studentId);
    }

    /**
     * 通过resultIds获取
     *
     * @param resultIds 结果Id
     * @return List<StatConclusion>
     */
    public List<StatConclusion> getByResultIds(List<Integer> resultIds) {
        return baseMapper.getByResultIds(resultIds);
    }

    /**
     * 获取指定时间的数据
     *
     * @param start 开始
     * @param end   结束
     * @return List<StatConclusion>
     */
    public List<StatConclusion> getByDate(Date start, Date end) {
        return baseMapper.getByDate(start, end);
    }

    /**
     * 获取学生跟踪预警列表
     *
     * @param pageRequest 分页请求
     * @param requestDTO  入参
     * @param schoolId    学校Id
     * @return IPage<StudentTrackWarningResponseDTO>
     */
    public IPage<StudentTrackWarningResponseDTO> getTrackList(PageRequest pageRequest, StudentTrackWarningRequestDTO requestDTO,
                                                              Integer schoolId) {
        return baseMapper.getTrackList(pageRequest.toPage(), requestDTO, schoolId);
    }

    /**
     * 通过筛查学生获取
     *
     * @param planStudentId 筛查学生
     * @return StatConclusion
     */
    public StatConclusion getByPlanStudentId(Integer planStudentId) {
        return baseMapper.getByPlanStudentId(planStudentId);
    }

    /**
     * 通过筛查结果id获取
     * @param resultId
     * @return
     */
    public StatConclusion getByResultId(Integer resultId){
        return baseMapper.getByResultId(resultId);
    }

    /**
     * 获取各个学校复查人数
     *
     * @param planId 计划Id
     * @param schoolIds 学校ID集合
     * @return Map<Integer, Integer>
     */
    public Map<Integer, Integer> getReviewCountMap(Integer planId, List<Integer> schoolIds) {
        return baseMapper.getSchoolReviewCountByPlanIdAndSchoolIds(planId, schoolIds).stream().collect(Collectors.toMap(SchoolCountDO::getSchoolId, SchoolCountDO::getSchoolCount));
    }

    /**
     * 统计各预警级别人数
     *
     * @param query 查询条件
     * @return List<WarningLevelCountDO>
     */
    public List<WarningLevelCountDO> countWarningLevel(StatConclusionQueryDTO query) {
        return baseMapper.countWarningLevel(query);
    }

    public List<StatConclusion> getByNoticeIdDistrictIds(Integer noticeId, List<Integer> districtIds) {
        if (CollectionUtils.isEmpty(districtIds)) {
            return Collections.emptyList();
        }
        return baseMapper.getByNoticeIdDistrictIds(noticeId, districtIds);
    }

    public List<StatConclusion> getByPlanIdSchoolId(Integer planId, Integer schoolId) {
        return baseMapper.getByPlanIdSchoolId(planId, schoolId);
    }

    public List<StatConclusion> getByPlanId(Integer planId) {
        return baseMapper.getByPlanId(planId);
    }

    public List<StatConclusion> getByDistrictIds(List<Integer> districtIds) {
        LambdaQueryWrapper<StatConclusion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(StatConclusion::getDistrictId, districtIds);
        return baseMapper.selectList(queryWrapper);
    }

    public List<StatConclusion> getByPlanStudentIds(List<Integer> planStudentIds) {
        LambdaQueryWrapper<StatConclusion> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.in(StatConclusion::getScreeningPlanSchoolStudentId,planStudentIds);
        queryWrapper.eq(StatConclusion::getIsRescreen,Boolean.FALSE);
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 获取学校预警等级为0-3的学生
     *
     * @param schoolId 学校
     *
     * @return 列表
     */
    public List<StatConclusion> getBySchoolIdAndWarningLevel(Integer schoolId) {
        return baseMapper.getBySchoolIdAndWarningLevel(schoolId);
    }

}

