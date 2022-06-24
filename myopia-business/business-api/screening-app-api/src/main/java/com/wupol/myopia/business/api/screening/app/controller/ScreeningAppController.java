package com.wupol.myopia.business.api.screening.app.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.excel.imports.CommonImportService;
import com.wupol.myopia.business.aggregation.screening.domain.dto.AppQueryQrCodeParams;
import com.wupol.myopia.business.aggregation.screening.domain.dto.UpdatePlanStudentRequestDTO;
import com.wupol.myopia.business.aggregation.screening.domain.vos.QrCodeInfo;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningExportService;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningPlanStudentBizService;
import com.wupol.myopia.business.aggregation.screening.service.VisionScreeningBizService;
import com.wupol.myopia.business.api.screening.app.domain.dto.*;
import com.wupol.myopia.business.api.screening.app.domain.vo.ClassScreeningProgress;
import com.wupol.myopia.business.api.screening.app.domain.vo.EyeDiseaseVO;
import com.wupol.myopia.business.api.screening.app.domain.vo.RescreeningResultVO;
import com.wupol.myopia.business.api.screening.app.domain.vo.ScreeningResultDataVO;
import com.wupol.myopia.business.api.screening.app.enums.ErrorEnum;
import com.wupol.myopia.business.api.screening.app.enums.SysEnum;
import com.wupol.myopia.business.api.screening.app.service.ScreeningAppService;
import com.wupol.myopia.business.api.screening.app.service.ScreeningPlanBizService;
import com.wupol.myopia.business.common.utils.constant.EyeDiseasesEnum;
import com.wupol.myopia.business.common.utils.constant.SourceClientEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dos.HeightAndWeightDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentScreeningProgressVO;
import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentVO;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationStaff;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationStaffService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
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
    @Autowired
    private ScreeningPlanStudentBizService screeningPlanStudentBizService;
    @Autowired
    private ScreeningExportService screeningExportService;
    @Autowired
    private CommonImportService commonImportService;
    @Autowired
    private ScreeningOrganizationStaffService screeningOrganizationStaffService;

    /**
     * 模糊查询某个筛查机构下的学校的
     *
     * @param schoolName 模糊查询
     * @return
     */
    @GetMapping("/school/findAllLikeSchoolName")
    public List<School> getSchoolNameByNameLike(String schoolName) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return screeningPlanBizService.getSchoolByOrgId(schoolName, currentUser.getOrgId());
    }

    /**
     * 查询学校的年级名称
     *
     * @param schoolId 学校ID
     * @return
     */
    @GetMapping("/school/findAllGradeNameBySchoolName")
    public List<SchoolGrade> getGradeNameBySchoolName(@NotNull(message = "schoolId不能为空") Integer schoolId, boolean all) {
        if (all) {
            //查找全部的年级
            return schoolGradeService.getBySchoolId(schoolId);
        }
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = screeningPlanSchoolStudentService.getCurrentPlanStudentByOrgIdAndSchoolId(schoolId, CurrentUserUtil.getCurrentUser().getOrgId());
        if (CollectionUtils.isEmpty(screeningPlanSchoolStudents)) {
            return Collections.emptyList();
        }
        List<Integer> gradeIds = screeningPlanSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getGradeId).distinct().collect(Collectors.toList());
        return schoolGradeService.getByIds(gradeIds);
    }

    /**
     * 获取班级名称
     *
     * @param gradeId  年级ID
     * @return
     */
    @GetMapping("/school/findAllClazzNameBySchoolNameAndGradeName")
    public List<SchoolClass> getClassNameBySchoolNameAndGradeName(@NotNull(message = "gradeId不能为空") Integer gradeId, boolean all) {
        if (all) {
            return schoolClassService.getByGradeId(gradeId);
        }
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = screeningPlanSchoolStudentService.getCurrentPlanStudentByGradeIdAndScreeningOrgId(gradeId, CurrentUserUtil.getCurrentUser().getOrgId());
        if (CollectionUtils.isEmpty(screeningPlanSchoolStudents)) {
            return Collections.emptyList();
        }
        List<Integer> classIds = screeningPlanSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getClassId).distinct().collect(Collectors.toList());
        return schoolClassService.getByIds(classIds);
    }


    /**
     * 获取学校年级班级对应的学生名称
     *
     * @param schoolId  学校名称
     * @param gradeId   年级名称
     * @param classId   班级名称
     * @return
     */
    @GetMapping("/school/findAllStudentName")
    public Page<StudentVO> findAllStudentName(Integer schoolId, Integer gradeId, Integer classId, String nameLike,
                                              @RequestParam(value = "current", defaultValue = "1") Integer page,
                                              @RequestParam(value = "size", defaultValue = "60") Integer size) {
        ScreeningStudentQueryDTO screeningStudentQuery = new ScreeningStudentQueryDTO().setScreeningOrgId(CurrentUserUtil.getCurrentUser().getOrgId()).setNameLike(nameLike);
        if (Objects.nonNull(schoolId) && schoolId != -1) {
            screeningStudentQuery.setSchoolId(schoolId);
        }
        if (Objects.nonNull(gradeId) && gradeId != -1) {
            screeningStudentQuery.setGradeId(gradeId);
        }
        if (Objects.nonNull(classId) && classId != -1) {
            screeningStudentQuery.setClassId(classId);
        }
        IPage<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentPage = screeningPlanSchoolStudentService.getCurrentPlanScreeningStudentList(screeningStudentQuery, page, size);
        List<StudentVO> studentVOs = screeningPlanSchoolStudentPage.getRecords().stream().map(StudentVO::getInstance).collect(Collectors.toList());
        return new PageImpl<>(studentVOs, PageRequest.of(page - 1, size), screeningPlanSchoolStudentPage.getTotal());
    }

    /**
     * 获取学生的信息
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
        if (screeningPlanStudentBizService.isNotMatchScreeningTime(screeningPlanSchoolStudent)) {
            return ApiResult.failure(SysEnum.SYS_STUDENT_SCREENING_TIME_ERROR.getCode(), SysEnum.SYS_STUDENT_SCREENING_TIME_ERROR.getMessage());
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

    /**
     * 保存身高体重数据
     *
     * @return
     */
    @PostMapping("/eye/addHeightAndWeight")
    public void addHeightAndWeight(@Valid @RequestBody HeightAndWeightDataDTO heightAndWeightDataDTO) {
        if (heightAndWeightDataDTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(heightAndWeightDataDTO);
        }
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
            @RequestParam(value = "clazzName", required = false) String clazzName) {

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
        appStudentDTO.checkStudentInfo();
        appStudentDTO.setDeptId(CurrentUserUtil.getCurrentUser().getOrgId());
        ApiResult apiResult = screeningAppService.validStudentParam(appStudentDTO);
        if (apiResult != null) {
            return apiResult;
        }
        School school = schoolService.getBaseMapper().selectById(appStudentDTO.getSchoolId());
        if (school == null) {
            return ApiResult.failure(ErrorEnum.SYS_SCHOOL_IS_NOT_EXIST.getCode(), ErrorEnum.SYS_SCHOOL_IS_NOT_EXIST.getMessage());
        }
        Student student = screeningAppService.getStudent(CurrentUserUtil.getCurrentUser(), appStudentDTO);
        try {
            studentService.saveStudent(student);
            screeningAppService.insertSchoolStudent(student);
            commonImportService.insertSchoolStudent(Lists.newArrayList(student), SourceClientEnum.SCREENING_APP.type);
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
        screeningPlanBizService.insertWithStudent(CurrentUserUtil.getCurrentUser(), student, appStudentDTO.getGrade(), appStudentDTO.getClazz(), appStudentDTO.getSchoolName(), school.getSchoolNo(), school.getDistrictId(), appStudentDTO.getSchoolId().intValue(), currentPlan,appStudentDTO.getPassport());
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
     *
     * @param schoolId 学校名称
     * @param gradeId 年级名称
     * @param classId 班级名称
     * @param isFilter 是否启用过滤条件
     * @return com.wupol.myopia.business.api.screening.app.domain.vo.ClassScreeningProgress
     **/
    @GetMapping("/class/progress")
    public ClassScreeningProgress getClassScreeningProgress(@NotNull(message = "学校ID不能为空") Integer schoolId,
                                                            @NotNull(message = "年级ID不能为空") Integer gradeId,
                                                            @NotNull(message = "班级ID不能为空") Integer classId,
                                                            Boolean isFilter) {
        return screeningAppService.getClassScreeningProgress(schoolId, gradeId, classId, CurrentUserUtil.getCurrentUser().getOrgId(), isFilter);
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
        if (screeningPlanStudentBizService.isNotMatchScreeningTime(screeningPlanSchoolStudent)) {
            throw new BusinessException(SysEnum.SYS_STUDENT_SCREENING_TIME_ERROR.getMessage());
        }
        return StudentScreeningProgressVO.getInstanceWithDefault(screeningResult, studentVO, screeningPlanSchoolStudent);
    }

    /**
     * 获取电脑验光检查数据
     *
     * @param planStudentId 筛查计划学生ID
     * @return com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO
     **/
    @GetMapping("/getComputerOptometryData/{planStudentId}")
    public ComputerOptometryDTO getComputerOptometryData(@PathVariable Integer planStudentId) {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentId(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId());
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
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentId(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId());
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
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentId(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId());
        if (Objects.isNull(screeningResult)) {
            return new BiometricDataDTO();
        }
        return BiometricDataDTO.getInstance(screeningResult.getBiometricData());
    }

    /**
     * 获取小瞳验光检查数据
     *
     * @param planStudentId 筛查计划学生ID
     * @return com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO
     **/
    @GetMapping("/getPupilOptometryData/{planStudentId}")
    public PupilOptometryDTO getPupilOptometryData(@PathVariable Integer planStudentId) {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentId(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId());
        if (Objects.isNull(screeningResult)) {
            return new PupilOptometryDTO();
        }
        return PupilOptometryDTO.getInstance(screeningResult.getPupilOptometryData());
    }

    /**
     * 获取眼压检查数据
     *
     * @param planStudentId 筛查计划学生ID
     * @return com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO
     **/
    @GetMapping("/getEyePressureData/{planStudentId}")
    public EyePressureDataDTO getEyePressureData(@PathVariable Integer planStudentId) {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentId(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId());
        if (Objects.isNull(screeningResult)) {
            return new EyePressureDataDTO();
        }
        return EyePressureDataDTO.getInstance(screeningResult.getEyePressureData());
    }

    /**
     * 获取眼位、眼底、裂隙灯、盲及视力损害检查数据
     *
     * @param planStudentId 筛查计划学生ID
     * @return com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO
     **/
    @GetMapping("/getMultiCheckData/{planStudentId}")
    public MultiCheckDataDTO getMultiCheckData(@PathVariable Integer planStudentId) {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentId(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId());
        if (Objects.isNull(screeningResult)) {
            return new MultiCheckDataDTO();
        }
        return MultiCheckDataDTO.getInstance(screeningResult.getOcularInspectionData(), screeningResult.getFundusData(), screeningResult.getSlitLampData(), screeningResult.getVisualLossLevelData());
    }

    /**
     * 获取其他眼病检查数据
     *
     * @param planStudentId 筛查计划学生ID
     * @return com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO
     **/
    @GetMapping("/getOtherEyeDiseaseData/{planStudentId}")
    public OtherEyeDiseasesDTO getOtherEyeDiseaseData(@PathVariable Integer planStudentId) {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentId(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId());
        if (Objects.isNull(screeningResult)) {
            return new OtherEyeDiseasesDTO();
        }
        return OtherEyeDiseasesDTO.getInstance(screeningResult.getOtherEyeDiseases(), screeningResult.getSystemicDiseaseSymptom());
    }

    /**
     * 获取身高体重检查数据
     * @Author tastyb
     * @param planStudentId 筛查计划学生ID
     * @return com.wupol.myopia.business.core.screening.flow.domain.dos.HeightAndWeightDataDTO
     **/
    @GetMapping("/getHeightAndWeightData/{planStudentId}")
    public HeightAndWeightDataDTO getHeightAndWeightData(@PathVariable Integer planStudentId) {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentId(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId());
        if (Objects.isNull(screeningResult)) {
            return new HeightAndWeightDataDTO();
        }
        return HeightAndWeightDataDTO.getInstance(screeningResult.getHeightAndWeightData());
    }

    /**
     * 获取所有筛查数据
     *
     * @param planStudentId 筛查计划学生ID
     * @return com.wupol.myopia.business.api.screening.app.domain.vo.ScreeningResultDataVO
     **/
    @GetMapping("/data/{planStudentId}")
    public ScreeningResultDataVO getScreeningResultData(@PathVariable Integer planStudentId) {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentId(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId());
        return ScreeningResultDataVO.getInstance(screeningResult);
    }

    /**
     * 获取筛查机构对应的未完成筛查且有筛查数据的学校
     *
     * @return
     */
    @GetMapping("/getSchoolHasScreeningData")
    public List<School> getSchoolHasScreeningData() {
        Set<Integer> currentPlanIds = screeningPlanService.getCurrentPlanIds(CurrentUserUtil.getCurrentUser().getOrgId());
        if (CollectionUtils.isEmpty(currentPlanIds)) {
            return Collections.emptyList();
        }
        List<VisionScreeningResult> visionScreeningResults = visionScreeningResultService.getByPlanIdsOrderByUpdateTimeDesc(currentPlanIds);
        if (CollectionUtils.isEmpty(visionScreeningResults)) {
            return Collections.emptyList();
        }
        return schoolService.getSchoolByIds(visionScreeningResults.stream().map(VisionScreeningResult::getSchoolId).distinct().collect(Collectors.toList()));
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
        ScreeningPlanSchoolStudent planStudent = screeningPlanSchoolStudentService.getById(visionScreeningResults.get(0).getScreeningPlanSchoolStudentId());

        return planStudent.setSchoolName(schoolService.getById(planStudent.getSchoolId()).getName())
                .setGradeName(schoolGradeService.getById(planStudent.getGradeId()).getName())
                .setClassName(schoolClassService.getById(planStudent.getClassId()).getName());
    }

    /**
     * 更新筛查学生信息
     *
     * @param requestDTO 更新信息
     * @return void
     **/
    @PostMapping("/update/planStudent")
    public void updatePlanStudent(@RequestBody@Valid UpdatePlanStudentRequestDTO requestDTO) {
        screeningPlanStudentBizService.updatePlanStudent(requestDTO);
    }

    /**
     * 获取指定学生的二维码
     *
     * @param appQueryQrCodeParams
     * @return
     */
    @GetMapping("/export/QRCode")
    public List<QrCodeInfo> exportQRCode(@Valid AppQueryQrCodeParams appQueryQrCodeParams) {
        try {
            return screeningExportService.getQrCodeAndStudentInfo(appQueryQrCodeParams, CurrentUserUtil.getCurrentUser().getOrgId());
        } catch (Exception e) {
            log.error("获取二维码异常", e);
            throw new BusinessException("获取二维码异常");
        }
    }

    /**
     * 获取筛查人员类型
     *
     * @return true-自动生成的筛查人员 false-普通筛查人员
     */
    @GetMapping("/check/staffType")
    public Boolean checkStaffType() {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        ScreeningOrganizationStaff staff = screeningOrganizationStaffService.getStaffsByUserId(currentUser.getId());
        if (Objects.isNull(staff)) {
            throw new BusinessException("筛查人员信息异常");
        }
        return ScreeningOrganizationStaff.AUTO_CREATE_SCREENING_PERSONNEL == staff.getType();
    }
}
