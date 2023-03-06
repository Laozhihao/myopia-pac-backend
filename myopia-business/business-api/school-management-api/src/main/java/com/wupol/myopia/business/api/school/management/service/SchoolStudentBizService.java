package com.wupol.myopia.business.api.school.management.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.excel.imports.SchoolStudentExcelImportService;
import com.wupol.myopia.business.aggregation.student.domain.builder.SchoolStudentInfoBuilder;
import com.wupol.myopia.business.aggregation.student.domain.vo.GradeInfoVO;
import com.wupol.myopia.business.aggregation.student.service.SchoolFacade;
import com.wupol.myopia.business.aggregation.student.service.SchoolStudentFacade;
import com.wupol.myopia.business.api.school.management.domain.dto.EyeHealthResponseDTO;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.hospital.domain.dos.ReportAndRecordDO;
import com.wupol.myopia.business.core.hospital.service.MedicalReportService;
import com.wupol.myopia.business.core.school.domain.dto.SchoolClassDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeItemsDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolStudentQueryDTO;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentListResponseDTO;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentQueryBO;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentRequestDTO;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.domain.vo.SchoolStudentListVO;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.builder.ScreeningBizBuilder;
import com.wupol.myopia.business.core.screening.flow.domain.model.*;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.EyeDataUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private SchoolStudentFacade schoolStudentFacade;
    @Resource
    private SchoolClassService schoolClassService;
    @Resource
    private ScreeningPlanService screeningPlanService;

    /**
     * 获取学生列表
     *
     * @param pageRequest 分页请求
     * @param requestDTO  请求入参
     *
     * @return IPage<SchoolStudentListResponseDTO>
     */
    public IPage<SchoolStudentListVO> getSchoolStudentList(PageRequest pageRequest, SchoolStudentQueryDTO requestDTO) {

        TwoTuple<Boolean, Boolean> kindergartenAndPrimaryAbove = schoolStudentFacade.kindergartenAndPrimaryAbove(requestDTO.getSchoolId());
        SchoolStudentQueryBO schoolStudentQueryBO = SchoolStudentInfoBuilder.builderSchoolStudentQueryBO(requestDTO, kindergartenAndPrimaryAbove);

        IPage<SchoolStudent> schoolStudentPage = schoolStudentService.listByCondition(pageRequest, schoolStudentQueryBO);

        IPage<SchoolStudentListVO> responseDTO = new Page<>(schoolStudentPage.getCurrent(), schoolStudentPage.getSize(), schoolStudentPage.getTotal());

        List<SchoolStudent> schoolStudentList = schoolStudentPage.getRecords();
        if (CollectionUtils.isEmpty(schoolStudentList)) {
            return responseDTO;
        }

        List<SchoolStudentListVO> studentListVOList = schoolStudentList.stream().map(SchoolStudentInfoBuilder::buildSchoolStudentListVO).collect(Collectors.toList());

        responseDTO.setRecords(studentListVOList);

        return responseDTO;
    }

    /**
     * 保存学生
     *
     * @param schoolStudent 学生
     * @param schoolId      学校Id
     *
     * @return SchoolStudent
     */
    @Transactional(rollbackFor = Exception.class)
    public SchoolStudent saveStudent(SchoolStudent schoolStudent, Integer schoolId) {
        schoolStudent = schoolStudentFacade.validSchoolStudent(schoolStudent, schoolId);
        schoolStudent.setSourceClient(SourceClientEnum.SCHOOL.type);

        // 更新管理端的数据
        Integer managementStudentId = schoolStudentExcelImportService.updateManagementStudent(schoolStudent);
        schoolStudent.setStudentId(managementStudentId);
        // 回填视力数据
        backfillVisionInfo(schoolStudent);
        // 保存或者更新
        schoolStudentService.saveOrUpdate(schoolStudent);
        return schoolStudent;
    }

    /**
     * 回填视力信息
     *
     * @param schoolStudent 更新的学生
     */
    private void backfillVisionInfo(SchoolStudent schoolStudent) {
        if (Objects.isNull(schoolStudent.getId())) {
            return;
        }
        SchoolStudent oldSchoolStudent = schoolStudentService.getById(schoolStudent.getId());
        schoolStudent.setGlassesType(oldSchoolStudent.getGlassesType());
        schoolStudent.setVisionLabel(oldSchoolStudent.getVisionLabel());
        schoolStudent.setLowVision(oldSchoolStudent.getLowVision());
        schoolStudent.setMyopiaLevel(oldSchoolStudent.getMyopiaLevel());
        schoolStudent.setScreeningMyopia(oldSchoolStudent.getScreeningMyopia());
        schoolStudent.setHyperopiaLevel(oldSchoolStudent.getHyperopiaLevel());
        schoolStudent.setAstigmatismLevel(oldSchoolStudent.getAstigmatismLevel());
        schoolStudent.setIsMyopia(oldSchoolStudent.getIsMyopia());
        schoolStudent.setIsHyperopia(oldSchoolStudent.getIsHyperopia());
        schoolStudent.setIsAstigmatism(oldSchoolStudent.getIsAstigmatism());
    }


    /**
     * 获取年级信息
     *
     * @param screeningPlanId 筛查计划ID
     * @param schoolId        学校ID
     */
    public List<GradeInfoVO> getGradeInfo(Integer screeningPlanId, Integer schoolId) {
        // 全部的年级+学生数
        TwoTuple<List<GradeInfoVO>, Map<Integer, List<Integer>>> gradeInfoAndSchoolStudent = getGradeInfoBySchoolId(schoolId);
        List<GradeInfoVO> gradeInfoVOList = gradeInfoAndSchoolStudent.getFirst();

        if (Objects.isNull(screeningPlanId)) {
            return gradeInfoVOList;
        }

        ScreeningPlanSchool screeningPlanSchool = screeningPlanSchoolService.getOneByPlanIdAndSchoolId(screeningPlanId, schoolId);
        if (Objects.isNull(screeningPlanSchool)) {
            throw new BusinessException("此筛查计划下没有此筛查学校");
        }
        List<Integer> screeningGradeIds = ScreeningBizBuilder.getScreeningGradeIds(screeningPlanSchool.getScreeningGradeIds());
        if (CollUtil.isEmpty(screeningGradeIds)) {
            // 全部年级未选中，则未选中的就是等于全部的
            gradeInfoVOList.forEach(x -> x.setUnSyncStudentNum(x.getStudentNum()).setIsSelect(false));
            return gradeInfoVOList;
        }
        // 设置是否选中
        gradeInfoVOList.forEach(x -> x.setIsSelect(screeningGradeIds.contains(x.getGradeId())));
        // 设置未同步到筛查计划学生列表的学生数量
        setUnSyncStudentNum(gradeInfoVOList, screeningPlanId, gradeInfoAndSchoolStudent.getSecond());
        return gradeInfoVOList;
    }

    /**
     * 根据学校ID查询学校年级信息
     * @param schoolId 学校ID
     */
    private TwoTuple<List<GradeInfoVO>, Map<Integer, List<Integer>>> getGradeInfoBySchoolId(Integer schoolId){
        //学生
        List<SchoolStudent> schoolStudentList = schoolStudentService.listBySchoolId(schoolId);
        Map<Integer, List<Integer>> gradeStudentIdMap = schoolStudentList.stream().collect(Collectors.groupingBy(SchoolStudent::getGradeId, Collectors.mapping(SchoolStudent::getStudentId, Collectors.toList())));
        //年级
        List<SchoolGrade> schoolGradeList = schoolGradeService.listBySchoolId(schoolId);
        //构建
        return new TwoTuple<>(schoolGradeList.stream().map(schoolGrade -> buildGradeInfo(gradeStudentIdMap, schoolGrade)).collect(Collectors.toList()), gradeStudentIdMap);
    }

    /**
     * 构建年级信息
     * @param gradeStudentIdMap 年级学生集合
     * @param schoolGrade 学校年级
     */
    private GradeInfoVO buildGradeInfo(Map<Integer, List<Integer>> gradeStudentIdMap, SchoolGrade schoolGrade) {
        List<Integer> schoolStudentList = gradeStudentIdMap.getOrDefault(schoolGrade.getId(), Lists.newArrayList());
        GradeInfoVO gradeInfoVO = new GradeInfoVO();
        gradeInfoVO.setGradeId(schoolGrade.getId());
        gradeInfoVO.setGradeName(schoolGrade.getName());
        gradeInfoVO.setStudentNum(schoolStudentList.size());
        return gradeInfoVO;
    }


    /**
     * 设置未同步到筛查计划学生列表的学生数量（求差集）
     *
     * @param gradeInfoList 年级信息对象
     * @param screeningPlanId  筛查计划ID
     * @param schoolStudentMap 学校学生Map
     */
    private void setUnSyncStudentNum(List<GradeInfoVO> gradeInfoList, Integer screeningPlanId, Map<Integer, List<Integer>> schoolStudentMap) {
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = screeningPlanSchoolStudentService.getByScreeningPlanId(screeningPlanId);
        Map<Integer, List<Integer>> planStudentMap = screeningPlanSchoolStudentList.stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getGradeId, Collectors.mapping(ScreeningPlanSchoolStudent::getStudentId, Collectors.toList())));
        gradeInfoList.forEach(x -> x.setUnSyncStudentNum(ListUtils.subtract(schoolStudentMap.getOrDefault(x.getGradeId(), Lists.newArrayList()), planStudentMap.getOrDefault(x.getGradeId(), Lists.newArrayList())).size()));
    }

    /**
     * 删除学生
     *
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

        List<Integer> studentIdList = schoolStudentService.listBySchoolId(schoolId).stream().map(SchoolStudent::getStudentId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(studentIdList)) {
            return new Page<>();
        }

        List<ReportAndRecordDO> visitLists = filterCreateTime(studentIdList);
        if (CollectionUtils.isEmpty(visitLists)) {
            if (Objects.equals(requestDTO.getIsHaveReport(), Boolean.TRUE)) {
                return new Page<>();
            }
        } else {
            List<Integer> haveReportStudentIds = visitLists.stream().map(ReportAndRecordDO::getStudentId).collect(Collectors.toList());
            if (Objects.nonNull(requestDTO.getIsHaveReport()) && CollectionUtils.isEmpty(haveReportStudentIds)) {
                return new Page<>();
            }
            requestDTO.setReportStudentIds(haveReportStudentIds);
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
        Map<Integer, List<ReportAndRecordDO>> visitMap = visitLists.stream().collect(Collectors.groupingBy(ReportAndRecordDO::getStudentId));

        page.setRecords(schoolStudents.stream().map(schoolStudent -> getEyeHealthResponseDTO(resultMap, statConclusionMap, schoolStudent, visitMap)).collect(Collectors.toList()));
        return page;
    }

    /**
     * 根据创建时间过滤
     *
     * @param studentIds 学生Id
     *
     * @return List<ReportAndRecordDO>
     */
    private List<ReportAndRecordDO> filterCreateTime(List<Integer> studentIds) {
        List<ReportAndRecordDO> visitLists = medicalReportService.getByStudentIds(studentIds);
        if (CollectionUtils.isEmpty(visitLists)) {
            return new ArrayList<>();
        }
        List<VisionScreeningResult> results = visionScreeningResultService.getByStudentIds(studentIds);
        if (CollectionUtils.isEmpty(results)) {
            return new ArrayList<>();
        }
        Map<Integer, VisionScreeningResult> resultMap = results.stream().collect(Collectors.toMap(VisionScreeningResult::getStudentId,
                Function.identity(),
                (v1, v2) -> v1.getCreateTime().after(v2.getCreateTime()) ? v1 : v2));

        List<Integer> planIds = results.stream().map(VisionScreeningResult::getPlanId).collect(Collectors.toList());
        Map<Integer, Date> planCreatTimeMap = screeningPlanService.getByIds(planIds).stream().collect(Collectors.toMap(ScreeningPlan::getId, ScreeningPlan::getStartTime));
        return visitLists.stream().filter(report -> {
            VisionScreeningResult visionScreeningResult = resultMap.get(report.getStudentId());
            if (Objects.isNull(visionScreeningResult)) {
                return false;
            }
            Date date = planCreatTimeMap.get(visionScreeningResult.getPlanId());
            if (Objects.isNull(date)) {
                return false;
            }
            return report.getCreateTime().after(date);
        }).collect(Collectors.toList());
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
        responseDTO.setWearingGlasses(Objects.nonNull(schoolStudent.getGlassesType()) ? WearingGlassesSituation.getType(schoolStudent.getGlassesType()) : null);

        boolean isKindergarten = SchoolAge.checkKindergarten(schoolStudent.getGradeType());
        if (isKindergarten) {
            responseDTO.setRefractiveResult(EyeDataUtil.getRefractiveResultDesc(statConclusion, true));
        } else {
            responseDTO.setRefractiveResult(EyeDataUtil.getRefractiveResultDesc(statConclusion, false));
        }
        responseDTO.setWarningLevel(WarningLevel.getDescByCode(schoolStudent.getVisionLabel()));

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
     *
     * @return List<SchoolGradeItemsDTO> 返回体
     */
    public List<SchoolGradeItemsDTO> getAllGradeList(Integer schoolId) {

        List<SchoolStudent> schoolStudents = schoolStudentService.getBySchoolIdAndVisionLabel(schoolId);
        if (CollectionUtils.isEmpty(schoolStudents)) {
            return new ArrayList<>();
        }
        List<SchoolGradeItemsDTO> schoolGrades = schoolGradeService.getAllByIds(schoolStudents.stream().map(SchoolStudent::getGradeId).collect(Collectors.toList()));
        if (CollectionUtils.isEmpty(schoolGrades)) {
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