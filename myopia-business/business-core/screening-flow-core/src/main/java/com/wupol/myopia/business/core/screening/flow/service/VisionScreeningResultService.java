package com.wupol.myopia.business.core.screening.flow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningSGCDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentScreeningCountDTO;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.VisionScreeningResultMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Service
public class VisionScreeningResultService extends BaseService<VisionScreeningResultMapper, VisionScreeningResult> {

    @Autowired
    private SchoolService schoolService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;

   /***
   * @Description: 学生ID集合
   * @Param: [studentIds]
   * @return: java.util.List<com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult>
   * @Author: 钓猫的小鱼
   * @Date: 2022/1/12
   */
    public List<VisionScreeningResult> getByStudentIds(Integer planId,List<Integer> studentIds) {
        return baseMapper.getByStudentIds(planId,studentIds);
    }

    /**
     * 通过StudentId获取筛查结果
     *
     * @param studentId id
     * @return List<ScreeningResult>
     */
    public List<VisionScreeningResult> getByStudentId(Integer studentId) {
        return baseMapper.getByStudentId(studentId);
    }

    /**
     * 获取筛查人员ID
     *
     * @param planId 计划od
     * @param orgId  机构ID
     * @return UserId
     */
    public List<Integer> getCreateUserIdByPlanId(Integer planId, Integer orgId) {
        return baseMapper.getCreateUserIdByPlanIdAndOrgId(planId, orgId);
    }

    /**
     * 获取学生筛查次数
     *
     * @return List<StudentScreeningCountVO>
     */
    public List<StudentScreeningCountDTO> countScreeningTime() {
        return baseMapper.countScreeningTime();
    }

    /**
     * 获取昨天筛查数据的筛查计划Id（必须有筛查通知，也就是省级配置的筛查机构筛查的数据）
     *
     * @return 筛查计划Id
     */
    public List<Integer> getYesterdayScreeningPlanIds() {
        Date yesterdayStartTime = DateUtil.getYesterdayStartTime();
        Date yesterdayEndTime = DateUtil.getYesterdayEndTime();
        return baseMapper.getHaveSrcScreeningNoticePlanIdsByTime(yesterdayStartTime, yesterdayEndTime);
    }

    /**
     * 根据筛查计划关联的存档的学生id
     *
     * @param screeningPlanSchoolStudentIds 计划的学生ID
     * @return List<VisionScreeningResult>
     */
    public List<VisionScreeningResult> getByScreeningPlanSchoolStudentIds(Set<Integer> screeningPlanSchoolStudentIds) {
        LambdaQueryWrapper<VisionScreeningResult> visionScreeningResultLambdaQueryWrapper = new LambdaQueryWrapper<>();
        visionScreeningResultLambdaQueryWrapper.eq(VisionScreeningResult::getIsDoubleScreen,false).in(VisionScreeningResult::getScreeningPlanSchoolStudentId, screeningPlanSchoolStudentIds);
        return baseMapper.selectList(visionScreeningResultLambdaQueryWrapper);
    }

    /**
     * 根据筛查计划ID集查询
     *
     * @param planIds 计划的学生ID
     * @return List<VisionScreeningResult>
     */
    public List<VisionScreeningResult> getByPlanIdsOrderByUpdateTimeDesc(Set<Integer> planIds) {
        LambdaQueryWrapper<VisionScreeningResult> visionScreeningResultLambdaQueryWrapper = new LambdaQueryWrapper<>();
        visionScreeningResultLambdaQueryWrapper.eq(VisionScreeningResult::getIsDoubleScreen,false).in(VisionScreeningResult::getPlanId, planIds).orderByDesc(VisionScreeningResult::getUpdateTime);
        return baseMapper.selectList(visionScreeningResultLambdaQueryWrapper);
    }

    /**
     * 获取学生的最新筛查报告
     *
     * @param studentId 学生ID
     * @return VisionScreeningResult
     */
    public VisionScreeningResult getLatestResultByStudentId(Integer studentId) {
        return baseMapper.getLatestResultByStudentId(studentId);
    }

    /**
     * 是否需要更新
     *
     * @param planId 计划ID
     * @param screeningOrgId 筛查机构ID
     * @return List<VisionScreeningResult>
     */
    public List<VisionScreeningResult> getScreeningResult(Integer planId, Integer screeningOrgId, Integer screeningPlanSchoolStudentId) {
        VisionScreeningResult visionScreeningResultQuery = new VisionScreeningResult().setPlanId(planId).setScreeningPlanSchoolStudentId(screeningPlanSchoolStudentId).setScreeningOrgId(screeningOrgId);
        QueryWrapper<VisionScreeningResult> queryWrapper = getQueryWrapper(visionScreeningResultQuery);
        return list(queryWrapper);
    }

    /**
     * 保存并更新数据
     *
     * @param visionScreeningResult
     * @return
     * @throws IOException
     */
    public VisionScreeningResult saveOrUpdateStudentScreenData(VisionScreeningResult visionScreeningResult) {
        if (visionScreeningResult != null && visionScreeningResult.getId() != null) {
            //更新
            updateById(visionScreeningResult);
        } else {
            //创建
            save(visionScreeningResult);
        }
        return visionScreeningResult;
    }

    /**
     * 获取筛查结果
     *
     * @param schoolId 学校ID
     * @param orgId    机构ID
     * @param planId   计划ID
     * @return List<VisionScreeningResult> 筛查结果
     */
    public List<VisionScreeningResult> getBySchoolIdAndOrgIdAndPlanId(Integer schoolId, Integer orgId, Integer planId) {
        return baseMapper.getBySchoolIdAndOrgIdAndPlanId(schoolId, orgId, planId);
    }

    /**
     * 获取学生的筛查记录
     *
     * @return 学生筛查记录
     */
    public List<VisionScreeningResult> getStudentResults() {
        return baseMapper.getStudentResults();
    }

    /**
     * 通过筛查学生查询筛查结果
     *
     * @param planStudentId 筛查学生
     * @return 筛查结果
     */
    public VisionScreeningResult getByPlanStudentId(Integer planStudentId) {
        return baseMapper.getByPlanStudentId(planStudentId);
    }

    /**
     * 通过筛查学生查询筛查结果
     *
     * @param planStudentIds 筛查学生
     * @return 筛查结果
     */
    public List<VisionScreeningResult> getByPlanStudentIds(List<Integer> planStudentIds) {
        return baseMapper.getByPlanStudentIds(planStudentIds);
    }
    /**
    * @Description: 获取筛查计划下有数据的学校
    * @Param: [筛查计划ID, 机构ID]
    * @return: java.util.List<com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningSchoolDTO>
    * @Author: 钓猫的小鱼
    * @Date: 2022/1/20
    */
    public List<ScreeningSGCDTO> getSchoolInfoHaveDataByPlanIdAndOrgId(Integer planId, Integer orgId) {
        List<ScreeningSGCDTO> screeningSGCDTOS = baseMapper.getSchoolInfoByPlanIdAndOrgId(planId,orgId);
        List<Integer> schoolId = screeningSGCDTOS.stream().map(ScreeningSGCDTO::getId).collect(Collectors.toList());
        List<School> schools = schoolService.getByIds(schoolId);
        Map<Integer, List<School>> schoolsMap = schools.stream().collect(Collectors.groupingBy(School::getId));
        screeningSGCDTOS.forEach(vo->vo.setName(getSchoolName(schoolsMap, vo)));
        return screeningSGCDTOS;
    }

    private String getSchoolName(Map<Integer, List<School>> schoolsMap, ScreeningSGCDTO vo) {
        return schoolsMap.get(vo.getId()).get(0).getName();
    }

    /**
    * @Description: 获取筛查计划下的年级
    * @Param: [planId, orgId, schoolId]
    * @return: java.util.List<com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningSGCDTO>
    * @Author: 钓猫的小鱼
    * @Date: 2022/1/20
    */
    public List<ScreeningSGCDTO> getSchoolInfoHaveDataByPlanIdAndOrgId(Integer planId, Integer orgId, Integer schoolId) {
        List<ScreeningSGCDTO> screeningSGCDTOS =  baseMapper.getGradeInfoByPlanIdAndOrgId(planId,orgId,schoolId);
        List<Integer> gradeIds = screeningSGCDTOS.stream().map(ScreeningSGCDTO::getId).collect(Collectors.toList());
        List<SchoolGrade> schoolGrades = schoolGradeService.getByIds(gradeIds);
        Map<Integer, List<SchoolGrade>> schoolGradeMap = schoolGrades.stream().collect(Collectors.groupingBy(SchoolGrade::getId));
        screeningSGCDTOS.forEach(vo->vo.setName(getGradeName(schoolGradeMap, vo)));

        return screeningSGCDTOS;
    }

    @NotBlank(message = "年级名称不能为空")
    private String getGradeName(Map<Integer, List<SchoolGrade>> schoolGradeMap, ScreeningSGCDTO vo) {
        return schoolGradeMap.get(vo.getId()).get(0).getName();
    }

    /**
    * @Description: 获取筛查计划下的班级
    * @Param: [planId, orgId, schoolId, gradeId]
    * @return: java.util.List<com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningSGCDTO>
    * @Author: 钓猫的小鱼
    * @Date: 2022/1/20
    */
    public List<ScreeningSGCDTO> getSchoolInfoHaveDataByPlanIdAndOrgId(Integer planId, Integer orgId,Integer schoolId,Integer gradeId) {
        List<ScreeningSGCDTO> screeningSGCDTOS =  baseMapper.getClassInfoByPlanIdAndOrgId(planId,orgId,schoolId,gradeId);
        List<Integer> classIds = screeningSGCDTOS.stream().map(ScreeningSGCDTO::getId).collect(Collectors.toList());
        List<SchoolClass> schoolClasses = schoolClassService.getByIds(classIds);
        Map<Integer, List<SchoolClass>> schoolClassMap = schoolClasses.stream().collect(Collectors.groupingBy(SchoolClass::getId));
        screeningSGCDTOS.forEach(vo -> vo.setName(getClassName(schoolClassMap, vo)));
        return baseMapper.getClassInfoByPlanIdAndOrgId(planId,orgId,schoolId,gradeId);
    }

    @NotBlank(message = "班级名称不能为空")
    private String getClassName(Map<Integer, List<SchoolClass>> schoolClassMap, ScreeningSGCDTO vo) {
        return schoolClassMap.get(vo.getId()).get(0).getName();
    }


}
