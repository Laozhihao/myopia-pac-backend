package com.wupol.myopia.business.api.school.management.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.business.aggregation.export.excel.imports.SchoolStudentExcelImportService;
import com.wupol.myopia.business.aggregation.student.domain.vo.GradeInfoVO;
import com.wupol.myopia.business.aggregation.student.service.SchoolFacade;
import com.wupol.myopia.business.aggregation.student.service.StudentFacade;
import com.wupol.myopia.business.api.school.management.domain.dto.EyeHealthResponseDTO;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.hospital.domain.dos.ReportAndRecordDO;
import com.wupol.myopia.business.core.hospital.service.MedicalReportService;
import com.wupol.myopia.business.core.school.domain.dto.SchoolClassDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeItemsDTO;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentListResponseDTO;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentRequestDTO;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.builder.ScreeningBizBuilder;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentScreeningCountDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.facade.SchoolScreeningBizFacade;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.EyeDataUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
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

    private static final String VISION_NORMAL = "视力正常";

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
    private ScreeningPlanSchoolService screeningPlanSchoolService;
    @Resource
    private SchoolScreeningBizFacade schoolScreeningBizFacade;
    @Resource
    private SchoolClassService schoolClassService;

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
        schoolScreeningBizFacade.addScreeningStudent(schoolStudent,isAdd);
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

        List<Integer> screeningGradeIds = ScreeningBizBuilder.getScreeningGradeIds(screeningPlanSchool.getScreeningGradeIds());

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
        Map<Integer, List<ScreeningPlanSchoolStudent>> gradePlanSchoolStudentMap = screeningPlanSchoolStudentService.groupingByFunction(screeningPlanSchoolStudentList, ScreeningPlanSchoolStudent::getGradeId);
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

    /**
     * 获取眼健康列表
     *
     * @param schoolId    学校Id
     * @param pageRequest 分页请求
     * @param requestDTO  请求参数
     *
     * @return IPage<EyeHealthResponseDTO>
     */
    public IPage<EyeHealthResponseDTO> getEyeHealthList(Integer schoolId, PageRequest pageRequest, SchoolStudentRequestDTO requestDTO) {

        if (Objects.equals(requestDTO.getIsHaveReport(), Boolean.TRUE)) {
            // 是否就诊
            List<ReportAndRecordDO> visitLists = medicalReportService.getByStudentIds(schoolStudentService.listBySchoolId(schoolId).stream().map(SchoolStudent::getStudentId).collect(Collectors.toList()));
            if (CollectionUtils.isEmpty(visitLists)) {
                return new Page<>();
            }
            requestDTO.setHavaReportStudentIds(visitLists.stream().map(ReportAndRecordDO::getStudentId).collect(Collectors.toList()));
        }

        IPage<SchoolStudentListResponseDTO> studentListPage = schoolStudentService.getList(pageRequest, requestDTO, schoolId);
        List<SchoolStudentListResponseDTO> schoolStudents = studentListPage.getRecords();
        if (CollectionUtils.isEmpty(schoolStudents)) {
            return new Page<>();
        }
        IPage<EyeHealthResponseDTO> page = new Page<>();
        BeanUtils.copyProperties(studentListPage, page);
        List<Integer> studentIds = schoolStudents.stream().map(SchoolStudent::getStudentId).collect(Collectors.toList());
        TwoTuple<Map<Integer, VisionScreeningResult>, Map<Integer, StatConclusion>> resultStatMap = visionScreeningResultService.getStudentResultAndStatMap(studentIds);
        Map<Integer, VisionScreeningResult> resultMap = resultStatMap.getFirst();
        Map<Integer, StatConclusion> statConclusionMap = resultStatMap.getSecond();

        // 是否就诊
        List<ReportAndRecordDO> visitLists = medicalReportService.getByStudentIds(studentIds);
        Map<Integer, List<ReportAndRecordDO>> visitMap = visitLists.stream().collect(Collectors.groupingBy(ReportAndRecordDO::getStudentId));

        page.setRecords(schoolStudents.stream().map(schoolStudent -> getEyeHealthResponseDTO(resultMap, statConclusionMap, schoolStudent, visitMap)).collect(Collectors.toList()));
        return page;
    }

    /**
     * 获取导出数据
     *
     * @param resultMap         筛查结果
     * @param statConclusionMap 统计结果
     * @param schoolStudent     学生
     * @param visitMap          就诊Map
     *
     * @return EyeHealthResponseDTO
     */
    private static EyeHealthResponseDTO getEyeHealthResponseDTO(Map<Integer, VisionScreeningResult> resultMap,
                                                                Map<Integer, StatConclusion> statConclusionMap,
                                                                SchoolStudentListResponseDTO schoolStudent,
                                                                Map<Integer, List<ReportAndRecordDO>> visitMap) {
        VisionScreeningResult result = resultMap.get(schoolStudent.getStudentId());
        StatConclusion statConclusion = statConclusionMap.get(schoolStudent.getStudentId());

        EyeHealthResponseDTO responseDTO = new EyeHealthResponseDTO();
        responseDTO.setStudentId(schoolStudent.getStudentId());
        responseDTO.setSchoolStudentId(schoolStudent.getId());
        responseDTO.setSno(schoolStudent.getSno());
        responseDTO.setName(schoolStudent.getName());
        responseDTO.setGradeName(schoolStudent.getGradeName());
        responseDTO.setClassName(schoolStudent.getClassName());
        responseDTO.setWearingGlasses(Objects.nonNull(schoolStudent.getGlassesType()) ? GlassesTypeEnum.get(schoolStudent.getGlassesType()).getDesc() : null);

        boolean isKindergarten = SchoolAge.checkKindergarten(schoolStudent.getGradeType());
        if (isKindergarten) {
            responseDTO.setRefractiveResult(EyeDataUtil.getRefractiveResultDesc(statConclusion, true));
        } else {
            responseDTO.setRefractiveResult(EyeDataUtil.getRefractiveResultDesc(statConclusion, false));
        }
        responseDTO.setWarningLevel(WarningLevel.getDesc(schoolStudent.getVisionLabel()));

        if (Objects.nonNull(statConclusion)) {
            stat2Response(result, statConclusion, responseDTO, isKindergarten);
        }
        responseDTO.setIsBindMp(StringUtils.isNotBlank(schoolStudent.getMpParentPhone()));
        responseDTO.setScreeningTime(schoolStudent.getLastScreeningTime());
        responseDTO.setIsHaveReport(!CollectionUtils.isEmpty(visitMap.get(schoolStudent.getStudentId())));
        return responseDTO;
    }

    /**
     * 结论转返回值
     *
     * @param result         结果
     * @param statConclusion 结论
     * @param responseDTO    返回体
     * @param isKindergarten 是否幼儿园
     */
    private static void stat2Response(VisionScreeningResult result, StatConclusion statConclusion, EyeHealthResponseDTO responseDTO, boolean isKindergarten) {
        if (isKindergarten) {
            responseDTO.setLowVision(Objects.equals(statConclusion.getIsLowVision(), Boolean.TRUE) ? VisionConst.K_LOW_VISION : VISION_NORMAL);
        } else {
            responseDTO.setLowVision(Objects.equals(statConclusion.getIsLowVision(), Boolean.TRUE) ? VisionConst.P_LOW_VISION : VISION_NORMAL);
        }
        responseDTO.setVisionCorrection(Objects.nonNull(statConclusion.getVisionCorrection()) ? VisionCorrection.get(statConclusion.getVisionCorrection()).desc : null);
        responseDTO.setIsRecommendVisit(statConclusion.getIsRecommendVisit());

        responseDTO.setHeight(EyeDataUtil.heightToStr(result));
        if (StringUtils.isNotBlank(responseDTO.getHeight())) {
            responseDTO.setSeatSuggest(true);
            TwoTuple<String, String> deskChairSuggest = EyeDataUtil.getDeskChairSuggest(responseDTO.getHeight(), statConclusion.getSchoolAge());
            responseDTO.setDesk(deskChairSuggest.getFirst());
            responseDTO.setChair(deskChairSuggest.getSecond());
        }
        responseDTO.setHaveBlackboardDistance(Objects.equals(MyopiaLevelEnum.seatSuggest(statConclusion.getMyopiaLevel()), Boolean.TRUE));
    }

    /**
     * 获取有筛查数据的年级列表(没有分页)
     *
     * @param schoolId 学校id
     * @return List<SchoolGradeItemsDTO> 返回体
     */
    public List<SchoolGradeItemsDTO> getAllGradeList(Integer schoolId) {

        List<SchoolStudent> schoolStudents = schoolStudentService.getBySchoolIdAndVisionLabel(schoolId);

        List<SchoolGradeItemsDTO> schoolGrades = schoolGradeService.getAllByIds(schoolStudents.stream().map(SchoolStudent::getGradeId).collect(Collectors.toList()));
        if(CollectionUtils.isEmpty(schoolGrades)) {
            return new ArrayList<>();
        }
        Map<Integer, String> gradeMap = schoolGrades.stream().collect(Collectors.toMap(SchoolGradeItemsDTO::getId, SchoolGradeItemsDTO::getName));

        // 获取班级，并且封装成Map
        Map<Integer, List<SchoolClassDTO>> classMaps = schoolClassService.getClassDTOByIds(schoolStudents.stream().map(SchoolStudent::getClassId).collect(Collectors.toList()))
                .stream()
                .map(schoolClass -> schoolGradeService.getSchoolClassDTO(gradeMap, schoolClass))
                .collect(Collectors.groupingBy(SchoolClassDTO::getGradeId));
        schoolGrades.forEach(g -> {
            g.setChild(classMaps.get(g.getId()));
            g.setUniqueId(UUID.randomUUID().toString());
        });
        return schoolGrades;
    }

}