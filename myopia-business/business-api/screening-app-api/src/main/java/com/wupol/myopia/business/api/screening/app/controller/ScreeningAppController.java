package com.wupol.myopia.business.api.screening.app.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.screening.service.VisionScreeningBizService;
import com.wupol.myopia.business.api.screening.app.domain.dto.*;
import com.wupol.myopia.business.api.screening.app.domain.vo.*;
import com.wupol.myopia.business.api.screening.app.enums.ErrorEnum;
import com.wupol.myopia.business.api.screening.app.enums.SysEnum;
import com.wupol.myopia.business.api.screening.app.service.ScreeningAppService;
import com.wupol.myopia.business.api.screening.app.service.ScreeningPlanBizService;
import com.wupol.myopia.business.common.utils.constant.EyeDiseasesEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ComputerOptometryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultSearchDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentScreeningProgressVO;
import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentVO;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 筛查端App接口
 *
 * @Author Chikong
 * @Date 2021-01-21
 */
@Validated
@CrossOrigin
@ResponseResultBody
@Controller
@RequestMapping("/app/screening")
@Slf4j
public class ScreeningAppController {

    @Autowired
    private ScreeningAppService screeningAppService;
    @Autowired
    private VisionScreeningBizService visionScreeningBizService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private SchoolClassService schoolClassService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private ScreeningPlanBizService screeningPlanBizService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;

    /**
     * 模糊查询某个筛查机构下的学校的
     *
     * @param schoolName 模糊查询
     * @param deptId     机构id
     * @param isReview   是否复测
     * @return
     */
    @GetMapping("/school/findAllLikeSchoolName")
    public Set<String> getSchoolNameByNameLike(@RequestParam String schoolName, String deptId, Boolean isReview) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        List<School> schools = screeningPlanBizService.getSchoolByOrgId(schoolName, currentUser.getOrgId());
        return schools.stream().map(School::getName).collect(Collectors.toSet());
    }

    /**
     * 查询学校的年级名称
     *
     * @param schoolName 学校名
     * @param deptId     机构id
     * @return
     */
    @GetMapping("/school/findAllGradeNameBySchoolName")
    public Set<String> getGradeNameBySchoolName(@RequestParam String schoolName, @RequestParam Integer deptId, boolean all) {
        Set<String> gradeNameSet;
        if (all) {
            //查找全部的年级
            List<SchoolGrade> schoolGrades = schoolGradeService.getBySchoolName(schoolName);
            gradeNameSet = schoolGrades.stream().map(SchoolGrade::getName).collect(Collectors.toSet());
        } else {
            School school = schoolService.findOne(new School().setName(schoolName));
            List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = screeningPlanSchoolStudentService.getCurrentPlanStudentByOrgIdAndSchoolId(school.getId(), deptId);
            if (CollectionUtils.isEmpty(screeningPlanSchoolStudents)) {
                return Collections.emptySet();
            }
            List<Integer> gradeIds = screeningPlanSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getGradeId).collect(Collectors.toList());
            List<SchoolGrade> schoolGrades = schoolGradeService.getByIds(gradeIds);
            gradeNameSet = schoolGrades.stream().map(SchoolGrade::getName).collect(Collectors.toSet());
        }
        return gradeNameSet;
    }

    /**
     * 获取班级名称
     *
     * @param schoolName 学校名称
     * @param gradeName  年级名称
     * @param deptId     机构id
     * @return
     */
    @GetMapping("/school/findAllClazzNameBySchoolNameAndGradeName")
    public Set<String> getClassNameBySchoolNameAndGradeName(String schoolName, String gradeName, Integer deptId, boolean all) {
        Set<String> classNameSet;
        if (all) {
            List<SchoolClass> schoolClassList = schoolClassService.getBySchoolNameAndGradeName(schoolName, gradeName);
            classNameSet = schoolClassList.stream().map(SchoolClass::getName).collect(Collectors.toSet());
        } else {
            School school = schoolService.findOne(new School().setName(schoolName));
            List<SchoolGrade> gradeList = schoolGradeService.findByList(new SchoolGrade().setName(gradeName).setSchoolId(school.getId()));
            if (CollectionUtils.isEmpty(gradeList)) {
                return Collections.emptySet();
            }
            List<Integer> gradeIds = gradeList.stream().map(SchoolGrade::getId).collect(Collectors.toList());
            List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = screeningPlanSchoolStudentService.getCurrentPlanStudentBySchoolIdAndGradeIds(school.getId(), gradeIds, deptId);
            if (CollectionUtils.isEmpty(screeningPlanSchoolStudents)) {
                return Collections.emptySet();
            }
            List<Integer> classIds = screeningPlanSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getClassId).collect(Collectors.toList());
            List<SchoolClass> classList = schoolClassService.getByIds(classIds);
            classNameSet = classList.stream().map(SchoolClass::getName).collect(Collectors.toSet());
        }
        return classNameSet;
    }

    /**
     * 获取学生的信息：
     *
     * @param
     * @return
     */
    @GetMapping("/student/findOneById")
    public ApiResult getStudentById(@NotNull(message = "planStudentId不能为空") Integer planStudentId) {
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.findOne(new ScreeningPlanSchoolStudent().setId(planStudentId).setScreeningOrgId(CurrentUserUtil.getCurrentUser().getOrgId()));
        if (Objects.isNull(screeningPlanSchoolStudent)) {
            return ApiResult.failure(SysEnum.SYS_STUDENT_NULL.getCode(), SysEnum.SYS_STUDENT_NULL.getMessage());
        }
        return ApiResult.success(StudentVO.getInstance(screeningPlanSchoolStudent));
    }

    /**
     * 获取筛查就机构对应的学校
     *
     * @param deptId 机构id
     * @return
     */
    @GetMapping("/findSchoolByDeptId")
    public List<School> listSchoolByScreeningOrgId(Integer deptId) {
        //筛查机构未完成的学校的信息
        return screeningAppService.getSchoolByScreeningOrgId(deptId);
    }

    /**
     * 查询眼睛疾病
     *
     * @return
     */
    @PostMapping("/eye/findAllEyeDisease")
    public List<EyeDiseaseVO> getAllEyeDisease() {
        List<String> eyeDiseaseList = EyeDiseasesEnum.eyeDiseaseList;
        List<EyeDiseaseVO> leftEyeDiseaseVO = eyeDiseaseList.stream().map(eyeDisease -> {
            EyeDiseaseVO eyeDiseaseVO = new EyeDiseaseVO();
            eyeDiseaseVO.setEye("L");
            eyeDiseaseVO.setName(eyeDisease);
            eyeDiseaseVO.setCreateTime(new Date());
            eyeDiseaseVO.setId("1");
            return eyeDiseaseVO;
        }).collect(Collectors.toList());

        List<EyeDiseaseVO> rightEyeDiseaseVO = eyeDiseaseList.stream().map(eyeDisease -> {
            EyeDiseaseVO eyeDiseaseVO = new EyeDiseaseVO();
            eyeDiseaseVO.setEye("R");
            eyeDiseaseVO.setName(eyeDisease);
            eyeDiseaseVO.setCreateTime(new Date());
            eyeDiseaseVO.setId("1");
            return eyeDiseaseVO;
        }).collect(Collectors.toList());
        List<EyeDiseaseVO> allEyeDiseaseVos = new ArrayList<>();
        allEyeDiseaseVos.addAll(rightEyeDiseaseVO);
        allEyeDiseaseVos.addAll(leftEyeDiseaseVO);
        return allEyeDiseaseVos;
    }

    /**
     * 上传筛查机构用户的签名图片
     *
     * @param deptId 机构id
     * @param userId 用户id
     * @param file   签名
     * @return
     */
    @PostMapping("/uploadSignPic")
    public ApiResult uploadUserAutographImageWithUser(@RequestParam(value = "deptId") Long deptId,
                                              @RequestParam(value = "userId") Long userId,
                                              @RequestParam(value = "file") MultipartFile file) {
        return ApiResult.success(screeningAppService.uploadSignPic(CurrentUserUtil.getCurrentUser(), file));
    }

    /**
     * 获取用户的基本信息
     *
     * @return
     */
    @GetMapping("/getUserInfo")
    public AppUserInfo getUserInfo() {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return screeningAppService.getUserInfoByUser(currentUser);
    }

    /**
     * 人脸识别 暂时不做
     *
     * @return
     */
    @PostMapping("/recognitionFace")
    public void recognitionFace(Integer deptId, MultipartFile file) {
       // 暂时不用
    }

    /**
     * 保存视力筛查
     *
     * @return
     */
    @PostMapping("/eye/addVision")
    public void addStudentVision(@Valid @RequestBody VisionDataDTO visionDataDTO) {
        if (visionDataDTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(visionDataDTO);
        }
    }

    /**
     * 保存电脑验光
     *
     * @return
     */
    @PostMapping("/eye/addComputer")
    public void addStudentComputer(@Valid @RequestBody ComputerOptometryDTO computerOptometryDTO) {
        if (computerOptometryDTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(computerOptometryDTO);
        }
    }

    /**
     * 保存生物测量数据
     *
     * @return
     */
    @PostMapping("/eye/addBiology")
    public void addStudentBiology(@Valid @RequestBody BiometricDataDTO biometricDataDTO) {
        if (biometricDataDTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(biometricDataDTO);
        }
    }

    /**
     * 保存其他眼病
     *
     * @return
     */
    @PostMapping("/eye/addEyeDisease")
    public void addEyeDisease(@Valid @RequestBody OtherEyeDiseasesDTO otherEyeDiseasesDTO) {
        visionScreeningBizService.saveOrUpdateStudentScreenData(otherEyeDiseasesDTO);
    }

    /**
     * 保存眼位、裂隙灯、眼底、盲及视力损害等级检查数据
     *
     * @return
     */
    @PostMapping("/eye/addMultiCheck")
    public void addMultiCheck(@Valid @RequestBody MultiCheckDataDTO multiCheckDataDTO) {
        if (multiCheckDataDTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(multiCheckDataDTO);
        }
    }

    /**
     * 保存小瞳验光数据
     *
     * @return
     */
    @PostMapping("/eye/addPupilOptometry")
    public void addPupilOptometry(@Valid @RequestBody PupilOptometryDTO pupilOptometryDTO) {
        if (pupilOptometryDTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(pupilOptometryDTO);
        }
    }

    /**
     * 保存眼压数据
     *
     * @return
     */
    @PostMapping("/eye/addEyePressure")
    public void addEyePressure(@Valid @RequestBody EyePressureDataDTO eyePressureDataDTO) {
        if (eyePressureDataDTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(eyePressureDataDTO);
        }
    }

    //分割线----------------------

    /**
     * 获取学校年级班级对应的学生名称
     *
     * @param schoolName  学校名称
     * @param gradeName   年级名称
     * @param clazzName   班级名称
     * @param studentName 学生名称
     * @param deptId      机构id
     * @return
     */
    @GetMapping("/school/findAllStudentName")
    public Page<StudentVO> findAllStudentName(
            @RequestParam(value = "deptId") Integer deptId,
            @RequestParam(value = "schoolName") String schoolName,
            @RequestParam(value = "gradeName", required = false) String gradeName,
            @RequestParam(value = "clazzName", required = false) String clazzName,
            @RequestParam(value = "studentName", required = false) String studentName,
            @RequestParam(value = "current", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "60") Integer size) {
        //获取当前筛查机构正在执行的所有计划。
        Pageable pageable = PageRequest.of(page - 1, size);
        gradeName = StringUtils.isBlank(gradeName) ? null : gradeName;
        clazzName = StringUtils.isBlank(clazzName) ? null : clazzName;
        studentName = StringUtils.isBlank(studentName) ? null : studentName;
        schoolName = StringUtils.isBlank(schoolName) ? null : schoolName;

        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = new ScreeningPlanSchoolStudent();
        screeningPlanSchoolStudent
                .setScreeningOrgId(deptId)
                .setSchoolName(schoolName)
                .setClassName(clazzName)
                .setStudentName(studentName)
                .setGradeName(gradeName);
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = screeningPlanSchoolStudentService.listByEntityDescByCreateTime(screeningPlanSchoolStudent, page, size);
        List<StudentVO> studentVOs = screeningPlanSchoolStudents.stream().map(StudentVO::getInstance).collect(Collectors.toList());
        return new PageImpl<>(studentVOs, pageable, studentVOs.size());
    }

    /**
     * 随机获取学生复测质量控制
     *
     * @param
     * @return
     */
    @GetMapping("/student/findReviewRandom")
    public List<SysStudent> findAllNameReview(
            @RequestParam(value = "deptId") Integer deptId,
            @RequestParam(value = "schoolId") Integer schoolId,
            String studentName,
            Integer current,
            Integer size,
            @RequestParam boolean isRandom,
            @RequestParam(value = "gradeName", required = false) String gradeName,
            @RequestParam(value = "clazzName", required = false) String clazzName) throws JsonProcessingException {

        gradeName = StringUtils.isBlank(gradeName) ? null : gradeName;
        clazzName = StringUtils.isBlank(clazzName) ? null : clazzName;
        return screeningAppService.getStudentReview(schoolId, gradeName, clazzName, deptId, studentName, current, size, isRandom);
    }

    /**
     * 更新复测质控结果
     *
     * @return
     */
    @PostMapping("/eye/updateReviewResult")
    public void updateReviewResult(Integer eyeId) {
      //暂时不用
    }

    /**
     * 保存学生信息
     *
     * @return
     */
    @PostMapping("/student/save")
    public ApiResult saveStudent(@RequestBody AppStudentDTO appStudentDTO) throws ParseException {
        appStudentDTO.setDeptId(CurrentUserUtil.getCurrentUser().getOrgId());
        ApiResult apiResult = screeningAppService.validStudentParam(appStudentDTO);
        if (apiResult != null) {
            return apiResult;
        }
        School school = schoolService.getBaseMapper().selectById(appStudentDTO.getSchoolId());
        if (school == null) {
            return ApiResult.failure(ErrorEnum.SYS_SCHOOL_IS_NOT_EXIST.getCode(), ErrorEnum.SYS_SCHOOL_IS_NOT_EXIST.getMessage());
        }
        Student student = screeningAppService.getStudent(CurrentUserUtil.getCurrentUser(), appStudentDTO, school);
        try {
            studentService.saveStudent(student);
            //获取当前的计划
        } catch (Exception e) {
            // app 就是这么干的。
            return ApiResult.failure(ErrorEnum.UNKNOWN_ERROR.getCode(), e.getMessage());
        }
        ScreeningPlan currentPlan = screeningPlanService.getCurrentPlan(CurrentUserUtil.getCurrentUser().getOrgId(), appStudentDTO.getSchoolId().intValue());

        if (currentPlan == null) {
            log.error("根据orgId = [{}]，以及schoolId = [{}] 无法找到计划。", CurrentUserUtil.getCurrentUser().getOrgId(), appStudentDTO.getSchoolId());
            return ApiResult.failure(ErrorEnum.UNKNOWN_ERROR.getMessage());
        }
        screeningPlanBizService.insertWithStudent(CurrentUserUtil.getCurrentUser(), student, appStudentDTO.getGrade(), appStudentDTO.getClazz(), appStudentDTO.getSchoolName(), school.getSchoolNo(), school.getDistrictId(), appStudentDTO.getSchoolId().intValue(), currentPlan);
        return ApiResult.success();
    }

    /**
     * 搜索复测质控结果
     *
     * @return
     */
    @GetMapping("/eye/findAllReviewResult")
    public List<RescreeningResultVO> findAllReviewResult(
            @RequestParam Integer deptId,
            @RequestParam(value = "schoolId") Integer schoolId,
            @RequestParam(value = "gradeName", required = false) String gradeName,
            @RequestParam(value = "clazzName", required = false) String clazzName) {
        ScreeningResultSearchDTO screeningResultSearchDTO = new ScreeningResultSearchDTO();
        screeningResultSearchDTO.setClazzName(clazzName);
        screeningResultSearchDTO.setGradeName(gradeName).setSchoolId(schoolId).setDepId(deptId);
        return screeningAppService.getAllReviewResult(screeningResultSearchDTO);
    }


    /**
     * 获取班级总的筛查进度：汇总统计+每个学生的进度
     * TODO：暂时沿用山西版风格（差评），待改成通过ID获取
     *
     * @param schoolName 学校名称
     * @param gradeName 年级名称
     * @param clazzName 班级名称
     * @return com.wupol.myopia.business.api.screening.app.domain.vo.ClassScreeningProgress
     **/
    @GetMapping("/class/progress")
    public ClassScreeningProgress getClassScreeningProgress(@RequestParam(value = "schoolName") @NotBlank(message = "学校名称不能为空") String schoolName,
                                                            @RequestParam(value = "gradeName") @NotBlank(message = "年级名称不能为空") String gradeName,
                                                            @RequestParam(value = "clazzName") @NotBlank(message = "班级名称不能为空") String clazzName) {
        return screeningAppService.getClassScreeningProgress(schoolName, gradeName, clazzName, CurrentUserUtil.getCurrentUser().getOrgId());
    }

    /**
     * 获取单个学生的筛查进度信息
     *
     * @param planStudentId 筛查计划学生ID
     * @return com.wupol.myopia.business.core.screening.flow.domain.vo.StudentScreeningProgressVO
     **/
    @GetMapping("/student/progress/{planStudentId}")
    public StudentScreeningProgressVO getStudentScreeningProgress(@PathVariable Integer planStudentId) {
        // TODO：考虑复筛？
        VisionScreeningResult screeningResult = visionScreeningResultService.findOne(new VisionScreeningResult().setScreeningPlanSchoolStudentId(planStudentId).setIsDoubleScreen(false));
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.getById(planStudentId);
        StudentVO studentVO = StudentVO.getInstance(screeningPlanSchoolStudent);
        return StudentScreeningProgressVO.getInstanceWithDefault(screeningResult, studentVO);
    }

    /**
     * 获取电脑验光检查数据
     *
     * @param planStudentId 筛查计划学生ID
     * @return com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO
     **/
    @GetMapping("/getComputerOptometryData/{planStudentId}")
    public ComputerOptometryDTO getComputerOptometryData(@PathVariable Integer planStudentId) {
        VisionScreeningResult screeningResult = visionScreeningResultService.findOne(new VisionScreeningResult().setScreeningPlanSchoolStudentId(planStudentId).setIsDoubleScreen(false));
        if (Objects.isNull(screeningResult)) {
            return new ComputerOptometryDTO();
        }
        return ComputerOptometryDTO.getInstance(screeningResult.getComputerOptometry());
    }

    /**
     * 获取视力检查数据
     *
     * @param planStudentId 筛查计划学生ID
     * @return com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO
     **/
    @GetMapping("/getVisionData/{planStudentId}")
    public VisionDataDTO getVisionData(@PathVariable Integer planStudentId) {
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.findOne(new ScreeningPlanSchoolStudent().setId(planStudentId).setScreeningOrgId(CurrentUserUtil.getCurrentUser().getOrgId()));
        Assert.notNull(screeningPlanSchoolStudent, SysEnum.SYS_STUDENT_NULL.getMessage());
        VisionScreeningResult screeningResult = visionScreeningResultService.findOne(new VisionScreeningResult().setScreeningPlanSchoolStudentId(planStudentId).setIsDoubleScreen(false));
        if (Objects.isNull(screeningResult)) {
            return new VisionDataDTO();
        }
        return VisionDataDTO.getInstance(screeningResult.getVisionData());
    }

    /**
     * 获取生物测量检查数据
     *
     * @param planStudentId 筛查计划学生ID
     * @return com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO
     **/
    @GetMapping("/getBiologyData/{planStudentId}")
    public BiometricDataDTO getBiologyData(@PathVariable Integer planStudentId) {
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.findOne(new ScreeningPlanSchoolStudent().setId(planStudentId).setScreeningOrgId(CurrentUserUtil.getCurrentUser().getOrgId()));
        Assert.notNull(screeningPlanSchoolStudent, SysEnum.SYS_STUDENT_NULL.getMessage());
        VisionScreeningResult screeningResult = visionScreeningResultService.findOne(new VisionScreeningResult().setScreeningPlanSchoolStudentId(planStudentId).setIsDoubleScreen(false));
        if (Objects.isNull(screeningResult)) {
            return new BiometricDataDTO();
        }
        return BiometricDataDTO.getInstance(screeningResult.getBiometricData());
    }

    /**
     * 获取筛查机构对应的未完成筛查且有筛查数据的学校
     *
     * @return
     */
    @GetMapping("/getSchoolHasScreeningData")
    public Set<String> getSchoolHasScreeningData() {
        Set<Integer> currentPlanIds = screeningPlanService.getCurrentPlanIds(CurrentUserUtil.getCurrentUser().getOrgId());
        if (CollectionUtils.isEmpty(currentPlanIds)) {
            return new HashSet<>();
        }
        List<VisionScreeningResult> visionScreeningResults = visionScreeningResultService.getByPlanIdsOrderByUpdateTimeDesc(currentPlanIds);
        if (CollectionUtils.isEmpty(visionScreeningResults)) {
            return new HashSet<>();
        }
        List<School> schools = schoolService.getSchoolByIds(visionScreeningResults.stream().map(VisionScreeningResult::getSchoolId).distinct().collect(Collectors.toList()));
        return schools.stream().map(School::getName).collect(Collectors.toSet());
    }


    /**
     * 获取最新一条筛查记录的学生信息
     *
     * @return
     */
    @GetMapping("/getLatestScreeningStudent")
    public ScreeningPlanSchoolStudent getLatestScreeningStudent() {
        Set<Integer> currentPlanIds = screeningPlanService.getCurrentPlanIds(CurrentUserUtil.getCurrentUser().getOrgId());
        if (CollectionUtils.isEmpty(currentPlanIds)) {
            return new ScreeningPlanSchoolStudent();
        }
        List<VisionScreeningResult> visionScreeningResults = visionScreeningResultService.getByPlanIdsOrderByUpdateTimeDesc(currentPlanIds);
        if (CollectionUtils.isEmpty(visionScreeningResults)) {
            return new ScreeningPlanSchoolStudent();
        }
        return screeningPlanSchoolStudentService.getById(visionScreeningResults.get(0).getScreeningPlanSchoolStudentId()) ;
    }

}
