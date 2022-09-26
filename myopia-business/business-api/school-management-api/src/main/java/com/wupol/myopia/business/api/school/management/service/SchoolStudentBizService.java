package com.wupol.myopia.business.api.school.management.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.excel.imports.SchoolStudentExcelImportService;
import com.wupol.myopia.business.aggregation.student.domain.vo.GradeInfoVO;
import com.wupol.myopia.business.aggregation.student.service.SchoolFacade;
import com.wupol.myopia.business.aggregation.student.service.StudentFacade;
import com.wupol.myopia.business.api.school.management.domain.builder.SchoolScreeningBizBuilder;
import com.wupol.myopia.business.common.utils.constant.SourceClientEnum;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.hospital.domain.dos.ReportAndRecordDO;
import com.wupol.myopia.business.core.hospital.service.MedicalReportService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentListResponseDTO;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentRequestDTO;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningOrgTypeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentScreeningCountDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 学校端-学生
 *
 * @author Simple4H
 */
@Slf4j
@Service
public class SchoolStudentBizService {

    @Resource
    private SchoolStudentService schoolStudentService;

    @Resource
    private VisionScreeningResultService visionScreeningResultService;

    @Resource
    private MedicalReportService medicalReportService;

    @Resource
    private SchoolStudentExcelImportService schoolStudentExcelImportService;

    @Resource
    private StudentFacade studentFacade;
    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Resource
    private StudentService studentService;
    @Resource
    private SchoolFacade schoolFacade;
    @Resource
    private SchoolGradeService schoolGradeService;
    @Resource
    private SchoolService schoolService;
    @Resource
    private ScreeningPlanSchoolService screeningPlanSchoolService;
    @Resource
    private ScreeningPlanService screeningPlanService;
    @Resource
    private VisionScreeningService visionScreeningService;

    /**
     * 获取学生列表
     *
     * @param pageRequest 分页请求
     * @param requestDTO  请求入参
     * @param schoolId    学校Id
     * @return IPage<SchoolStudentListResponseDTO>
     */
    public IPage<SchoolStudentListResponseDTO> getList(PageRequest pageRequest, SchoolStudentRequestDTO requestDTO, Integer schoolId) {

        IPage<SchoolStudentListResponseDTO> responseDTO = schoolStudentService.getList(pageRequest, requestDTO, schoolId);
        List<SchoolStudentListResponseDTO> studentList = responseDTO.getRecords();

        // 学生Ids
        List<Integer> studentIds = studentList.stream().map(SchoolStudent::getStudentId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(studentIds)) {
            return responseDTO;
        }

        // 筛查次数
        List<StudentScreeningCountDTO> studentScreeningCountVOS = visionScreeningResultService.getVisionScreeningCountBySchoolId(schoolId);
        Map<Integer, StudentScreeningCountDTO> countMaps = studentScreeningCountVOS.stream().collect(Collectors
                .toMap(StudentScreeningCountDTO::getStudentId, Function.identity()));

        // 获取就诊记录
        List<ReportAndRecordDO> visitLists = medicalReportService.getByStudentIds(studentIds);
        Map<Integer, List<ReportAndRecordDO>> visitMap = visitLists.stream()
                .collect(Collectors.groupingBy(ReportAndRecordDO::getStudentId));

        studentList.forEach(s -> {
            s.setScreeningCount(Optional.ofNullable(countMaps.get(s.getStudentId())).map(StudentScreeningCountDTO::getCount).orElse(0));
            s.setNumOfVisits(Objects.nonNull(visitMap.get(s.getStudentId())) ? visitMap.get(s.getStudentId()).size() : 0);
            // 由于作废计划的存在，需要动态获取最新的非作废计划的筛查数据更新时间，覆盖固化的最新筛查日期
            s.setLastScreeningTime(Optional.ofNullable(countMaps.get(s.getStudentId())).map(StudentScreeningCountDTO::getUpdateTime).orElse(null));
        });
        return responseDTO;
    }

    /**
     * 保存学生
     *
     * @param schoolStudent 学生
     * @param schoolId      学校Id
     * @return SchoolStudent
     */
    @Transactional(rollbackFor = Exception.class)
    public SchoolStudent saveStudent(SchoolStudent schoolStudent, Integer schoolId) {
        validSchoolStudent(schoolStudent);
        studentFacade.setSchoolStudentInfo(schoolStudent, schoolId);

        // 更新管理端的数据
        Integer managementStudentId = schoolStudentExcelImportService.updateManagementStudent(schoolStudent);
        schoolStudent.setStudentId(managementStudentId);
        schoolStudent.setSourceClient(SourceClientEnum.SCHOOL.type);

        boolean isAdd = Objects.isNull(schoolStudent.getId());
        schoolStudentService.saveOrUpdate(schoolStudent);
        addScreeningStudent(schoolStudent,isAdd);
        return schoolStudent;
    }

    /**
     * 校验学校学生信息
     * @param schoolStudent 学校学生
     */
    private void validSchoolStudent(SchoolStudent schoolStudent) {
        Assert.isTrue(StrUtil.isNotBlank(schoolStudent.getSno()),"学号不能为空");
        Assert.isTrue(StrUtil.isNotBlank(schoolStudent.getName()),"姓名不能为空");
        Assert.isTrue(Objects.nonNull(schoolStudent.getGender()),"性别不能为空");
        Assert.isTrue(Objects.nonNull(schoolStudent.getGradeId()),"年级不能为空");
        Assert.isTrue(Objects.nonNull(schoolStudent.getClassId()),"班级不能为空");
        Assert.isTrue(Objects.nonNull(schoolStudent.getBirthday()),"出生日期不能为空");
    }

    /**
     * 往筛查计划新增学生 (自动新增)
     * @param schoolStudent 学校学生
     * @param isAdd 是否新增的学生
     */
    @Transactional(rollbackFor = Exception.class)
    public void addScreeningStudent(SchoolStudent schoolStudent,Boolean isAdd){

        if (Objects.equals(Boolean.FALSE,isAdd) || Objects.isNull(schoolStudent)){
            return;
        }
        //获取有效的筛查计划
        List<ScreeningPlan> screeningPlanList = getEffectiveScreeningPlans(schoolStudent);
        if (CollUtil.isEmpty(screeningPlanList)) {
            return;
        }
        Set<Integer> planIds = screeningPlanList.stream().map(ScreeningPlan::getId).collect(Collectors.toSet());
        List<ScreeningPlanSchool> screeningPlanSchoolList = screeningPlanSchoolService.listByPlanIdsAndSchoolId(Lists.newArrayList(planIds), schoolStudent.getSchoolId());
        Map<Integer, ScreeningPlanSchool> planSchoolMap = screeningPlanSchoolList.stream().collect(Collectors.toMap(ScreeningPlanSchool::getScreeningPlanId, Function.identity()));

        //组装数据，更新筛查计划学生数和新增筛查学生
        School school = schoolService.getById(schoolStudent.getSchoolId());
        screeningPlanList.forEach(screeningPlan -> addScreeningStudent(schoolStudent, planSchoolMap, school, screeningPlan));

    }

    /**
     * 获取有效的筛查计划
     * @param schoolStudent 学校学生
     */
    private List<ScreeningPlan> getEffectiveScreeningPlans(SchoolStudent schoolStudent) {
        //机构ID和机构类型查询筛查计划
        List<ScreeningPlan> screeningPlanList = screeningPlanService.getByOrgIdAndOrgType(schoolStudent.getSchoolId(), ScreeningOrgTypeEnum.SCHOOL.getType());
        if (CollUtil.isEmpty(screeningPlanList)){
            return Lists.newArrayList();
        }
        //获取有效期的筛查计划
        screeningPlanList = screeningPlanList.stream()
                .filter(screeningPlan -> DateUtil.isIn(new Date(), screeningPlan.getStartTime(), screeningPlan.getEndTime()))
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(screeningPlanList)){
            return Lists.newArrayList();
        }
        return screeningPlanList;
    }

    /**
     * 往筛查计划新增学生 (自动新增)
     * @param schoolStudent
     * @param planSchoolMap
     * @param school
     * @param screeningPlan
     */
    public void addScreeningStudent(SchoolStudent schoolStudent, Map<Integer, ScreeningPlanSchool> planSchoolMap, School school, ScreeningPlan screeningPlan) {
        ScreeningPlanSchool screeningPlanSchool = planSchoolMap.get(screeningPlan.getId());
        if (Objects.isNull(screeningPlanSchool)){
            return;
        }
        List<Integer> screeningGradeIds = SchoolScreeningBizBuilder.getScreeningGradeIds(screeningPlanSchool.getScreeningGradeIds());
        if (!screeningGradeIds.contains(schoolStudent.getGradeId())){
            return;
        }
        TwoTuple<List<ScreeningPlanSchoolStudent>, List<Integer>> screeningPlanSchoolStudent = visionScreeningService.getScreeningPlanSchoolStudent(screeningPlan.getId(), Lists.newArrayList(schoolStudent), school, Boolean.TRUE);
        screeningPlan.setStudentNumbers(screeningPlan.getStudentNumbers()+screeningPlanSchoolStudent.getFirst().size());
        screeningPlanService.savePlanInfo(screeningPlan,null,screeningPlanSchoolStudent);
        Object[] paramArr = new Object[]{screeningPlan.getId(),school.getId(),schoolStudent.getGradeId(),schoolStudent.getId()};
        log.info("自动新增筛查学生，plan={},schoolId={},gradeId={},schoolStudentId={}",paramArr);
    }

    /**
     * 获取年级信息
     * @param screeningPlanId 筛查计划ID
     * @param schoolId 学校ID
     */
    public GradeInfoVO getGradeInfo(Integer screeningPlanId, Integer schoolId) {
        GradeInfoVO gradeInfoVO = new GradeInfoVO();

        //全部的年级+学生数
        List<GradeInfoVO.GradeInfo> gradeInfoVOList = schoolFacade.getGradeInfoBySchoolId(schoolId);
        gradeInfoVO.setAllList(gradeInfoVOList);

        if (Objects.isNull(screeningPlanId)){
            return gradeInfoVO;
        }

        ScreeningPlanSchool screeningPlanSchool = screeningPlanSchoolService.getOneByPlanIdAndSchoolId(screeningPlanId, schoolId);
        if (Objects.isNull(screeningPlanSchool)){
            throw new BusinessException("此筛查计划下没有此筛查学校");
        }

        List<Integer> screeningGradeIds = SchoolScreeningBizBuilder.getScreeningGradeIds(screeningPlanSchool.getScreeningGradeIds());

        if (CollUtil.isEmpty(screeningGradeIds)){
            //已选中的为空，未选中的就是等于全部的
            gradeInfoVO.setNoSelectList(gradeInfoVOList);
            return gradeInfoVO;
        }
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = screeningPlanSchoolStudentService.getByScreeningPlanId(screeningPlanId);
        //已选中的年级+学生数
        planGradeInfoList(gradeInfoVO, screeningPlanSchoolStudentList,screeningGradeIds);

        //未选中的年级+学生数（全部的-已选中的）
        setNoSelectStudent(gradeInfoVO, gradeInfoVOList, screeningGradeIds);
        return gradeInfoVO;
    }



    /**
     * 设置未选中的年级+学生数
     * @param gradeInfoVO 年级信息对象
     * @param gradeInfoVOList 全部的年级+学生数对象集合
     * @param screeningGradeIds 选中的年级ID集合
     */
    private void setNoSelectStudent(GradeInfoVO gradeInfoVO, List<GradeInfoVO.GradeInfo> gradeInfoVOList, List<Integer> screeningGradeIds) {
        //未选中年级+学生数
        List<GradeInfoVO.GradeInfo> noSelectList = Lists.newArrayList();
        gradeInfoVOList.forEach(gradeInfo -> {
            if (!screeningGradeIds.contains(gradeInfo.getGradeId())){
                noSelectList.add(gradeInfo);
            }
        });
        gradeInfoVO.setNoSelectList(noSelectList);
    }


    /**
     * 获取已选中的年级+学生数
     * @param gradeInfoVO 年级信息对象
     * @param screeningPlanSchoolStudentList 筛查计划学生集合
     */
    private void planGradeInfoList(GradeInfoVO gradeInfoVO, List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList,List<Integer> screeningGradeIds) {
        Map<Integer, List<ScreeningPlanSchoolStudent>> gradePlanSchoolStudentMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(screeningPlanSchoolStudentList)){
            Map<Integer, List<ScreeningPlanSchoolStudent>> map = screeningPlanSchoolStudentList.stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getGradeId));
            gradePlanSchoolStudentMap.putAll(map);
        }
        List<SchoolGrade> schoolGradeList = schoolGradeService.listByIds(screeningGradeIds);
        Map<Integer, SchoolGrade> gradeMap = schoolGradeList.stream().collect(Collectors.toMap(SchoolGrade::getId, Function.identity()));
        List<GradeInfoVO.GradeInfo> planGradeInfoList = screeningGradeIds.stream().map(gradeId -> buildGradeInfo(gradeId, gradeMap,gradePlanSchoolStudentMap)).collect(Collectors.toList());
        gradeInfoVO.setSelectList(planGradeInfoList);
    }

    /**
     * 构建年级信息对象
     * @param gradeId 年级ID
     * @param gradeMap 年级集合
     * @param gradePlanSchoolStudentMap 年级学生集合
     */
    private GradeInfoVO.GradeInfo buildGradeInfo(Integer gradeId, Map<Integer, SchoolGrade> gradeMap,Map<Integer, List<ScreeningPlanSchoolStudent>> gradePlanSchoolStudentMap) {
        GradeInfoVO.GradeInfo gradeInfo = new GradeInfoVO.GradeInfo();
        gradeInfo.setGradeId(gradeId);
        gradeInfo.setGradeName(gradeMap.get(gradeId).getName());
        gradeInfo.setStudentNum(gradePlanSchoolStudentMap.getOrDefault(gradeId,Lists.newArrayList()).size());
        return gradeInfo;
    }

    /**
     * 删除学生
     * @param id 学生ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletedStudent(Integer id) {
        SchoolStudent schoolStudent = schoolStudentService.getById(id);
        Integer studentId = schoolStudent.getStudentId();
        if (screeningPlanSchoolStudentService.checkStudentHavePlan(studentId)) {
            throw new BusinessException("该学生有对应的筛查计划，无法进行删除");
        }
        schoolStudentService.deletedStudent(id);
        studentService.deletedStudent(studentId);
    }

}