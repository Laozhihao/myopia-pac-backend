package com.wupol.myopia.business.api.school.management.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.excel.imports.SchoolStudentExcelImportService;
import com.wupol.myopia.business.aggregation.student.domain.vo.GradeInfoVO;
import com.wupol.myopia.business.aggregation.student.service.SchoolFacade;
import com.wupol.myopia.business.aggregation.student.service.StudentFacade;
import com.wupol.myopia.business.common.utils.constant.SourceClientEnum;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.hospital.domain.dos.ReportAndRecordDO;
import com.wupol.myopia.business.core.hospital.service.MedicalReportService;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentListResponseDTO;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentRequestDTO;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentScreeningCountDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 学校端-学生
 *
 * @author Simple4H
 */
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
        studentFacade.setSchoolStudentInfo(schoolStudent, schoolId);

        // 更新管理端的数据
        Integer managementStudentId = schoolStudentExcelImportService.updateManagementStudent(schoolStudent);
        schoolStudent.setStudentId(managementStudentId);
        schoolStudent.setSourceClient(SourceClientEnum.SCHOOL.type);

        schoolStudentService.saveOrUpdate(schoolStudent);
        return schoolStudent;
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
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = screeningPlanSchoolStudentService.getByScreeningPlanId(screeningPlanId);
        if (CollUtil.isEmpty(screeningPlanSchoolStudentList)){
            //已选中的为空，未选中的就是等于全部的
            gradeInfoVO.setNoSelectList(gradeInfoVOList);
            return gradeInfoVO;
        }

        //已选中的年级+学生数
        List<GradeInfoVO.GradeInfo> planGradeInfoList = getGradeInfos(gradeInfoVO, screeningPlanSchoolStudentList);

        //未选中的年级+学生数（全部的-已选中的）
        setNoSelectStudent(gradeInfoVO, gradeInfoVOList, planGradeInfoList);
        return gradeInfoVO;
    }

    /**
     * 设置未选中的年级+学生数
     * @param gradeInfoVO 年级信息对象
     * @param gradeInfoVOList 全部的年级+学生数对象集合
     * @param planGradeInfoList 选中的年级+学生数对象集合
     */
    private void setNoSelectStudent(GradeInfoVO gradeInfoVO, List<GradeInfoVO.GradeInfo> gradeInfoVOList, List<GradeInfoVO.GradeInfo> planGradeInfoList) {
        Map<Integer, GradeInfoVO.GradeInfo> planGradeInfoMap = planGradeInfoList.stream().collect(Collectors.toMap(GradeInfoVO.GradeInfo::getGradeId, Function.identity()));
        //未选中年级+学生数
        List<GradeInfoVO.GradeInfo> noSelectList = Lists.newArrayList();
        gradeInfoVOList.forEach(gradeInfo -> getNoSelect(planGradeInfoMap, noSelectList, gradeInfo));
        gradeInfoVO.setNoSelectList(noSelectList);
    }

    /**
     * 获取未选中的年级+学生数
     * @param planGradeInfoMap 选中的年级+学生数对象集合
     * @param noSelectList 未选中年级+学生数
     * @param gradeInfo 全部的年级+学生数
     */
    private void getNoSelect(Map<Integer, GradeInfoVO.GradeInfo> planGradeInfoMap, List<GradeInfoVO.GradeInfo> noSelectList, GradeInfoVO.GradeInfo gradeInfo) {
        GradeInfoVO.GradeInfo planGradeInfo = planGradeInfoMap.get(gradeInfo.getGradeId());
        if (Objects.isNull(planGradeInfo)){
            noSelectList.add(gradeInfo);
            return;
        }
        int noSelectNum = gradeInfo.getStudentNum() - planGradeInfo.getStudentNum();
        if (noSelectNum > 0){
            GradeInfoVO.GradeInfo noSelect = ObjectUtil.cloneByStream(gradeInfo);
            noSelect.setStudentNum(noSelectNum);
            noSelectList.add(noSelect);
        }
    }

    /**
     * 获取已选中的年级+学生数
     * @param gradeInfoVO 年级信息对象
     * @param screeningPlanSchoolStudentList 筛查计划学生集合
     */
    private List<GradeInfoVO.GradeInfo> getGradeInfos(GradeInfoVO gradeInfoVO, List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList) {
        Map<Integer, List<ScreeningPlanSchoolStudent>> gradePlanSchoolStudentMap = screeningPlanSchoolStudentList.stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getGradeId));
        List<SchoolGrade> schoolGradeList = schoolGradeService.listByIds(gradePlanSchoolStudentMap.keySet());
        Map<Integer, SchoolGrade> gradeMap = schoolGradeList.stream().collect(Collectors.toMap(SchoolGrade::getId, Function.identity()));
        List<GradeInfoVO.GradeInfo> planGradeInfoList = gradePlanSchoolStudentMap.entrySet().stream().map(entry -> buildGradeInfo(entry, gradeMap)).collect(Collectors.toList());
        gradeInfoVO.setSelectList(planGradeInfoList);
        return planGradeInfoList;
    }

    /**
     * 构建年级信息对象
     * @param entry 筛查计划的学生集合
     * @param gradeMap 年级集合
     */
    private GradeInfoVO.GradeInfo buildGradeInfo(Map.Entry<Integer, List<ScreeningPlanSchoolStudent>> entry, Map<Integer, SchoolGrade> gradeMap) {
        GradeInfoVO.GradeInfo gradeInfo = new GradeInfoVO.GradeInfo();
        gradeInfo.setGradeId(entry.getKey());
        gradeInfo.setGradeName(gradeMap.get(entry.getKey()).getName());
        gradeInfo.setStudentNum(entry.getValue().size());
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