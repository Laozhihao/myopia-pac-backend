package com.wupol.myopia.business.api.screening.app.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import com.wupol.myopia.business.api.screening.app.domain.vo.*;
import com.wupol.myopia.business.api.screening.app.enums.ErrorEnum;
import com.wupol.myopia.business.api.screening.app.enums.SysEnum;
import com.wupol.myopia.business.api.screening.app.service.ScreeningAppService;
import com.wupol.myopia.business.api.screening.app.service.ScreeningPlanBizService;
import com.wupol.myopia.business.common.utils.constant.EyeDiseasesEnum;
import com.wupol.myopia.business.common.utils.constant.SourceClientEnum;
import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dos.DeviationDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.HeightAndWeightDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ComputerOptometryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultSearchDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentScreeningProgressVO;
import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentVO;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
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
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
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
    private ScreeningPlanSchoolService screeningPlanSchoolService;

    /**
     * 模糊查询某个筛查机构下的学校的
     *
     * @param schoolName 模糊查询
     * @return
     */
    @GetMapping("/school/findAllLikeSchoolName")
    public List<School> getSchoolNameByNameLike(String schoolName, @RequestParam(value = "channel", defaultValue = "0") Integer channel) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return screeningPlanBizService.getSchoolByOrgId(schoolName, currentUser.getOrgId(), channel);
    }

    /**
     * 查询学校的年级名称
     *
     * @param schoolId 学校ID
     * @return
     */
    @GetMapping("/school/findAllGradeNameBySchoolName")
    public List<SchoolGrade> getGradeNameBySchoolName(@NotNull(message = "schoolId不能为空") Integer schoolId, boolean all, @RequestParam(value = "channel", defaultValue = "0") Integer channel) {
        if (all) {
            //查找全部的年级
            return schoolGradeService.getBySchoolId(schoolId);
        }
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = screeningPlanSchoolStudentService.getCurrentPlanStudentByOrgIdAndSchoolId(schoolId, CurrentUserUtil.getCurrentUser().getOrgId(), channel);
        if (CollectionUtils.isEmpty(screeningPlanSchoolStudents)) {
            return Collections.emptyList();
        }
        List<Integer> gradeIds = screeningPlanSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getGradeId).distinct().collect(Collectors.toList());
        return schoolGradeService.getByIds(gradeIds);
    }

    /**
     * 获取班级名称
     *
     * @param gradeId 年级ID
     * @return
     */
    @GetMapping("/school/findAllClazzNameBySchoolNameAndGradeName")
    public List<SchoolClass> getClassNameBySchoolNameAndGradeName(@NotNull(message = "gradeId不能为空") Integer gradeId, boolean all, @RequestParam(value = "channel", defaultValue = "0") Integer channel) {
        if (all) {
            return schoolClassService.getByGradeId(gradeId);
        }
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = screeningPlanSchoolStudentService.getCurrentPlanStudentByGradeIdAndScreeningOrgId(gradeId, CurrentUserUtil.getCurrentUser().getOrgId(), channel);
        if (CollectionUtils.isEmpty(screeningPlanSchoolStudents)) {
            return Collections.emptyList();
        }
        List<Integer> classIds = screeningPlanSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getClassId).distinct().collect(Collectors.toList());
        return schoolClassService.getByIds(classIds);
    }


    /**
     * 获取学校年级班级对应的学生名称
     *
     * @param schoolId 学校名称
     * @param gradeId  年级名称
     * @param classId  班级名称
     * @return
     */
    @GetMapping("/school/findAllStudentName")
    public Page<StudentVO> findAllStudentName(Integer schoolId, Integer gradeId, Integer classId, String nameLike,
                                              @RequestParam(value = "channel", defaultValue = "0") Integer channel,
                                              @RequestParam(value = "current", defaultValue = "1") Integer page,
                                              @RequestParam(value = "size", defaultValue = "60") Integer size) {
        // 新版本不分页，这里需要兼容旧版本，数量为最大,学生数一般最多100,999比较合适
        if (channel == 1) {
            size = 999;
        }
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
        IPage<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentPage = screeningPlanSchoolStudentService.getCurrentPlanScreeningStudentList(screeningStudentQuery, page, size, channel);
        List<StudentVO> studentVOs = screeningPlanSchoolStudentPage.getRecords().stream()
                .sorted(Comparator.comparing(ScreeningPlanSchoolStudent::getCreateTime).reversed())
                .map(StudentVO::getInstance).collect(Collectors.toList());
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
        if (!screeningPlanStudentBizService.isMatchScreeningTime(screeningPlanSchoolStudent)) {
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
    public List<School> listSchoolByScreeningOrgId(Integer deptId, @RequestParam(value = "channel", defaultValue = "0") Integer channel) {
        //筛查机构未完成的学校的信息
        return screeningAppService.getSchoolByScreeningOrgId(deptId, channel);
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
    public ApiResult addStudentVision(@Valid @RequestBody VisionDataDTO visionDataDTO) {
        if (visionDataDTO.isValid()) {
            CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
            visionScreeningBizService.saveOrUpdateStudentScreenData(visionDataDTO,currentUser.getClientId());
            return ApiResult.success();
        } else {
            return ApiResult.failure("请输入正确的参数");
        }
    }

    /**
     * 保存电脑验光
     *
     * @return
     */
    @PostMapping("/eye/addComputer")
    public ApiResult addStudentComputer(@Valid @RequestBody ComputerOptometryDTO computerOptometryDTO) {
        if (computerOptometryDTO.isValid()) {
            CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
            visionScreeningBizService.saveOrUpdateStudentScreenData(computerOptometryDTO,currentUser.getClientId());
            return ApiResult.success();
        } else {
            return ApiResult.failure("请输入正确的参数");
        }
    }

    /**
     * 保存生物测量数据
     *
     * @return
     */
    @PostMapping("/eye/addBiology")
    public ApiResult addStudentBiology(@Valid @RequestBody BiometricDataDTO biometricDataDTO) {
        if (biometricDataDTO.isValid()) {
            CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
            visionScreeningBizService.saveOrUpdateStudentScreenData(biometricDataDTO,currentUser.getClientId());
            return ApiResult.success();
        } else {
            return ApiResult.failure("请输入正确的参数");
        }
    }

    /**
     * 保存其他眼病
     *
     * @return
     */
    @PostMapping("/eye/addEyeDisease")
    public void addEyeDisease(@Valid @RequestBody OtherEyeDiseasesDTO otherEyeDiseasesDTO) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        visionScreeningBizService.saveOrUpdateStudentScreenData(otherEyeDiseasesDTO,currentUser.getClientId());
    }

    /**
     * 保存眼位、裂隙灯、眼底、盲及视力损害等级检查数据
     *
     * @return
     */
    @PostMapping("/eye/addMultiCheck")
    public ApiResult addMultiCheck(@Valid @RequestBody MultiCheckDataDTO multiCheckDataDTO) {
        if (multiCheckDataDTO.isValid()) {
            CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
            visionScreeningBizService.saveOrUpdateStudentScreenData(multiCheckDataDTO,currentUser.getClientId());
            return ApiResult.success();
        } else {
            return ApiResult.failure("请输入正确的参数");
        }
    }

    /**
     * 保存小瞳验光数据
     *
     * @return
     */
    @PostMapping("/eye/addPupilOptometry")
    public ApiResult addPupilOptometry(@Valid @RequestBody PupilOptometryDTO pupilOptometryDTO) {
        if (pupilOptometryDTO.isValid()) {
            CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
            visionScreeningBizService.saveOrUpdateStudentScreenData(pupilOptometryDTO,currentUser.getClientId());
            return ApiResult.success();
        } else {
            return ApiResult.failure("请输入正确的参数");
        }
    }

    /**
     * 保存眼压数据
     *
     * @return
     */
    @PostMapping("/eye/addEyePressure")
    public ApiResult addEyePressure(@Valid @RequestBody EyePressureDataDTO eyePressureDataDTO) {
        if (eyePressureDataDTO.isValid()) {
            CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
            visionScreeningBizService.saveOrUpdateStudentScreenData(eyePressureDataDTO,currentUser.getClientId());
            return ApiResult.success();
        } else {
            return ApiResult.failure("请输入正确的参数");
        }
    }

    /**
     * 保存身高体重数据
     *
     * @return
     */
    @PostMapping("/eye/addHeightAndWeight")
    public ApiResult addHeightAndWeight(@Valid @RequestBody HeightAndWeightDataDTO heightAndWeightDataDTO) {
        if (heightAndWeightDataDTO.isValid()) {
            CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
            visionScreeningBizService.saveOrUpdateStudentScreenData(heightAndWeightDataDTO,currentUser.getClientId());
            return ApiResult.success();
        } else {
            return ApiResult.failure("请输入正确的参数");
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
            @RequestParam(value = "clazzName", required = false) String clazzName,
            @RequestParam(value = "channel", defaultValue = "0") Integer channel) throws JsonProcessingException {

        gradeName = StringUtils.isBlank(gradeName) ? null : gradeName;
        clazzName = StringUtils.isBlank(clazzName) ? null : clazzName;
        return screeningAppService.getStudentReview(schoolId, gradeName, clazzName, deptId, studentName, current, size, isRandom, channel);
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
    public ApiResult saveStudent(@RequestBody AppStudentDTO appStudentDTO, @RequestParam(value = "channel", defaultValue = "0") Integer channel) throws ParseException {
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
        Student student = screeningAppService.getStudent(CurrentUserUtil.getCurrentUser(), appStudentDTO, school);
        try {
            studentService.saveStudent(student);
            screeningAppService.insertSchoolStudent(student);
            commonImportService.insertSchoolStudent(Lists.newArrayList(student), SourceClientEnum.SCREENING_APP.type);
            //获取当前的计划
        } catch (Exception e) {
            // app 就是这么干的。
            return ApiResult.failure(ErrorEnum.UNKNOWN_ERROR.getCode(), e.getMessage());
        }
        ScreeningPlan currentPlan = screeningPlanService.getCurrentPlan(CurrentUserUtil.getCurrentUser().getOrgId(), appStudentDTO.getSchoolId().intValue(), channel);

        if (currentPlan == null) {
            log.error("根据orgId = [{}]，以及schoolId = [{}] 无法找到计划。", CurrentUserUtil.getCurrentUser().getOrgId(), appStudentDTO.getSchoolId());
            return ApiResult.failure(ErrorEnum.UNKNOWN_ERROR.getMessage());
        }
        screeningPlanBizService.insertWithStudent(CurrentUserUtil.getCurrentUser(), student, appStudentDTO.getGrade(), appStudentDTO.getClazz(), appStudentDTO.getSchoolName(), school.getSchoolNo(), school.getDistrictId(), appStudentDTO.getSchoolId().intValue(), currentPlan, appStudentDTO.getPassport());
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
     * @param gradeId  年级名称
     * @param classId  班级名称
     * @param isFilter 是否启用过滤条件
     * @return com.wupol.myopia.business.api.screening.app.domain.vo.ClassScreeningProgress
     **/
    @GetMapping("/class/progress")
    public ClassScreeningProgress getClassScreeningProgress(@NotNull(message = "学校ID不能为空") Integer schoolId,
                                                            @NotNull(message = "年级ID不能为空") Integer gradeId,
                                                            @NotNull(message = "班级ID不能为空") Integer classId,
                                                            @RequestParam(value = "isState", defaultValue = "0") Integer isState,
                                                            Boolean isFilter,
                                                            @RequestParam(value = "channel", defaultValue = "0") Integer channel) {
        return screeningAppService.getClassScreeningProgress(schoolId, gradeId, classId, CurrentUserUtil.getCurrentUser().getOrgId(), isFilter, isState, channel);
    }

    /**
     * 获取单个学生的筛查进度信息
     *
     * @param planStudentId 筛查计划学生ID
     * @return com.wupol.myopia.business.core.screening.flow.domain.vo.StudentScreeningProgressVO
     **/
    @GetMapping("/student/progress/{planStudentId}")
    public StudentScreeningProgressVO getStudentScreeningProgress(
            @PathVariable Integer planStudentId,
            @RequestParam(value = "isState", defaultValue = "0") Integer isState,
            @RequestParam(value = "channel", defaultValue = "0") Integer channel) {
        VisionScreeningResult screeningResult = visionScreeningResultService.findOne(new VisionScreeningResult()
                .setScreeningPlanSchoolStudentId(planStudentId)
                .setIsDoubleScreen(isState == 1)
                .setScreeningType(channel)
        );
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.getById(planStudentId);
        StudentVO studentVO = StudentVO.getInstance(screeningPlanSchoolStudent);
        if (!screeningPlanStudentBizService.isMatchScreeningTime(screeningPlanSchoolStudent)) {
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
    public ApiResult getComputerOptometryData(@PathVariable Integer planStudentId, @RequestParam(value = "isState", defaultValue = "0") Integer isState) {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentIdAndState(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId(), isState);
        if (Objects.isNull(screeningResult)) {
            return ApiResult.success();
        }
        return ApiResult.success(ComputerOptometryDTO.getInstance(screeningResult.getComputerOptometry()));
    }

    /**
     * 获取视力检查数据
     *
     * @param planStudentId 筛查计划学生ID
     * @return com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO
     **/
    @GetMapping("/getVisionData/{planStudentId}")
    public ApiResult getVisionData(@PathVariable Integer planStudentId, @RequestParam(value = "isState", defaultValue = "0") Integer isState) {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentIdAndState(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId(), isState);
        if (Objects.isNull(screeningResult)) {
            return ApiResult.success();
        }
        return ApiResult.success(VisionDataDTO.getInstance(screeningResult.getVisionData()));
    }

    /**
     * 获取生物测量检查数据
     *
     * @param planStudentId 筛查计划学生ID
     * @return com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO
     **/
    @GetMapping("/getBiologyData/{planStudentId}")
    public ApiResult getBiologyData(@PathVariable Integer planStudentId) {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentId(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId());
        if (Objects.isNull(screeningResult)) {
            return ApiResult.success();
        }
        return ApiResult.success(BiometricDataDTO.getInstance(screeningResult.getBiometricData()));
    }

    /**
     * 获取小瞳验光检查数据
     *
     * @param planStudentId 筛查计划学生ID
     * @return com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO
     **/
    @GetMapping("/getPupilOptometryData/{planStudentId}")
    public ApiResult getPupilOptometryData(@PathVariable Integer planStudentId) {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentId(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId());
        if (Objects.isNull(screeningResult)) {
            return ApiResult.success();
        }
        return ApiResult.success(PupilOptometryDTO.getInstance(screeningResult.getPupilOptometryData()));
    }

    /**
     * 获取眼压检查数据
     *
     * @param planStudentId 筛查计划学生ID
     * @return com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO
     **/
    @GetMapping("/getEyePressureData/{planStudentId}")
    public ApiResult getEyePressureData(@PathVariable Integer planStudentId) {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentId(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId());
        if (Objects.isNull(screeningResult)) {
            return ApiResult.success();
        }
        return ApiResult.success(EyePressureDataDTO.getInstance(screeningResult.getEyePressureData()));
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
    public OtherEyeDiseasesDTO getOtherEyeDiseaseData(@PathVariable Integer planStudentId, @RequestParam(value = "isState", defaultValue = "0") Integer isState) {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentIdAndState(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId(), isState);
        if (Objects.isNull(screeningResult)) {
            return new OtherEyeDiseasesDTO();
        }
        return OtherEyeDiseasesDTO.getInstance(screeningResult.getOtherEyeDiseases(), screeningResult.getSystemicDiseaseSymptom());
    }

    /**
     * 获取身高体重检查数据
     *
     * @param planStudentId 筛查计划学生ID
     * @return com.wupol.myopia.business.core.screening.flow.domain.dos.HeightAndWeightDataDTO
     * @Author tastyb
     **/
    @GetMapping("/getHeightAndWeightData/{planStudentId}")
    public HeightAndWeightDataDTO getHeightAndWeightData(@PathVariable Integer planStudentId, @RequestParam(value = "isState", defaultValue = "0") Integer isState) {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentIdAndState(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId(), isState);
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
    public ApiResult getScreeningResultData(@PathVariable Integer planStudentId, @RequestParam(value = "isState", defaultValue = "0") Integer isState) {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentIdAndState(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId(), isState);
        ScreeningResultDataVO result = ScreeningResultDataVO.getInstance(screeningResult);
        if (isState == 1) {
            VisionScreeningResult screeningResultFirst = screeningAppService.getVisionScreeningResultByPlanStudentIdAndState(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId(), 0);
            ScreeningResultDataVO resultFirst = ScreeningResultDataVO.getInstance(screeningResultFirst);
            if (Objects.isNull(result.getVisionData())) {
                VisionDataDTO visionDataDTO = new VisionDataDTO();
                visionDataDTO.setGlassesType(resultFirst.getVisionData().getGlassesType());
                result.setVisionData(visionDataDTO);
            } else {
                result.getVisionData().setGlassesType(resultFirst.getVisionData().getGlassesType());
            }
        }
        return ApiResult.success(result);
    }

    /**
     * 获取筛查机构对应的未完成筛查且有筛查数据的学校
     *
     * @return
     */
    @GetMapping("/getSchoolHasScreeningData")
    public List<School> getSchoolHasScreeningData(@RequestParam(value = "channel", defaultValue = "0") Integer channel) {
        Set<Integer> currentPlanIds = screeningPlanService.getCurrentPlanIds(CurrentUserUtil.getCurrentUser().getOrgId(), channel);
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
    public ScreeningPlanSchoolStudent getLatestScreeningStudent(@RequestParam(value = "channel", defaultValue = "0") Integer channel) {
        Set<Integer> currentPlanIds = screeningPlanService.getCurrentPlanIds(CurrentUserUtil.getCurrentUser().getOrgId(), channel);
        if (CollectionUtils.isEmpty(currentPlanIds)) {
            return new ScreeningPlanSchoolStudent();
        }

        List<VisionScreeningResult> visionScreeningResults = visionScreeningResultService.getByPlanIdsOrderByUpdateTimeDesc(currentPlanIds);
        if (CollectionUtils.isEmpty(visionScreeningResults)) {
            List<ScreeningPlanSchool> schoolPlan = screeningPlanSchoolService.getSchoolListsByPlanId(Lists.newArrayList(currentPlanIds).get(0));
            if (Objects.nonNull(schoolPlan) && !schoolPlan.isEmpty()) {
                ScreeningPlanSchoolStudent planStudent = screeningPlanSchoolStudentService.getOneByPlanId(Lists.newArrayList(currentPlanIds).get(0));
                if (Objects.nonNull(planStudent)) {
                    return planStudent.setSchoolName(schoolService.getById(planStudent.getSchoolId()).getName())
                            .setGradeName(schoolGradeService.getById(planStudent.getGradeId()).getName())
                            .setClassName(schoolClassService.getById(planStudent.getClassId()).getName())
                            .setGradeId(planStudent.getGradeId())
                            .setSchoolId(planStudent.getSchoolId())
                            .setGradeId(planStudent.getClassId());
                } else {
                    planStudent = new ScreeningPlanSchoolStudent();
                    return planStudent
                            .setSchoolId(schoolPlan.get(0).getSchoolId())
                            .setSchoolName(schoolService.getById(schoolPlan.get(0).getSchoolId()).getName());
                }
            } else {
                return new ScreeningPlanSchoolStudent();
            }
        }
        ScreeningPlanSchoolStudent planStudent = screeningPlanSchoolStudentService.getById(visionScreeningResults.get(0).getScreeningPlanSchoolStudentId());

        return planStudent.setSchoolName(schoolService.getById(planStudent.getSchoolId()).getName())
                .setGradeName(schoolGradeService.getById(planStudent.getGradeId()).getName())
                .setClassName(schoolClassService.getById(planStudent.getClassId()).getName())
                .setGradeId(planStudent.getGradeId())
                .setSchoolId(planStudent.getSchoolId())
                .setGradeId(planStudent.getClassId());
    }

    /**
     * 更新筛查学生信息
     *
     * @param requestDTO 更新信息
     * @return void
     **/
    @PostMapping("/update/planStudent")
    public void updatePlanStudent(@RequestBody @Valid UpdatePlanStudentRequestDTO requestDTO) {
        screeningPlanStudentBizService.updatePlanStudent(requestDTO);
    }

    /**
     * 获取指定学生的二维码
     *
     * @param appQueryQrCodeParams
     * @return
     */
    @GetMapping("/export/QRCode")
    public List<QrCodeInfo> exportQRCode(@Valid AppQueryQrCodeParams appQueryQrCodeParams, @RequestParam(value = "channel", defaultValue = "0") Integer channel) {
        try {
            return screeningExportService.getQrCodeAndStudentInfo(appQueryQrCodeParams, CurrentUserUtil.getCurrentUser().getOrgId(), channel);
        } catch (Exception e) {
            log.error("获取二维码异常", e);
            throw new BusinessException("获取二维码异常");
        }
    }

    /**
     * 常见病：龋齿数据保存
     *
     * @param saprodontiaDTO saprodontiaDTO
     */
    @PostMapping("/saprodontia")
    public ApiResult addSaprodontia(@Valid @RequestBody SaprodontiaDTO saprodontiaDTO) {
        if (saprodontiaDTO.isValid()) {
            CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
            visionScreeningBizService.saveOrUpdateStudentScreenData(saprodontiaDTO,currentUser.getClientId());
            return ApiResult.success();
        } else {
            return ApiResult.failure("请输入正确的参数");
        }
    }

    /**
     * 获取龋齿数据
     *
     * @param planStudentId 筛查计划学生ID
     * @return com.wupol.myopia.business.core.screening.flow.domain.dos.SaprodontiaDataDO
     **/
    @GetMapping("/saprodontia/{planStudentId}")
    public ApiResult getSaprodontia(@PathVariable Integer planStudentId) {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentId(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId());
        if (Objects.isNull(screeningResult)) {
            return ApiResult.success();
        }
        return ApiResult.success(SaprodontiaDTO.getInstance(screeningResult.getSaprodontiaData()));
    }

    /**
     * 常见病：脊柱数据保存
     *
     * @param spineDTO spineDto
     */
    @PostMapping("/spine")
    public ApiResult addSpine(@Valid @RequestBody SpineDTO spineDTO) {
        if (spineDTO.isValid()) {
            CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
            visionScreeningBizService.saveOrUpdateStudentScreenData(spineDTO,currentUser.getClientId());
            return ApiResult.success();
        } else {
            return ApiResult.failure("请输入正确的参数");
        }
    }

    /**
     * 获取脊柱数据
     *
     * @param planStudentId 筛查计划学生ID
     * @return com.wupol.myopia.business.core.screening.flow.domain.dos.SpineDataDO
     **/
    @GetMapping("/spine/{planStudentId}")
    public ApiResult getSpine(@PathVariable Integer planStudentId) {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentId(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId());
        if (Objects.isNull(screeningResult)) {
            return ApiResult.success();
        }
        return ApiResult.success(SpineDTO.getInstance(screeningResult.getSpineData()));
    }

    /**
     * 常见病：血压数据保存
     *
     * @param bloodPressureDTO bloodPressureDTO
     */
    @PostMapping("/bloodPressure")
    public ApiResult addBloodPressure(@Valid @RequestBody BloodPressureDTO bloodPressureDTO) {
        if (bloodPressureDTO.isValid()) {
            CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
            visionScreeningBizService.saveOrUpdateStudentScreenData(bloodPressureDTO,currentUser.getClientId());
            return ApiResult.success();
        } else {
            return ApiResult.failure("请输入正确的参数");
        }
    }

    /**
     * 获取血压数据
     *
     * @param planStudentId 筛查计划学生ID
     * @return com.wupol.myopia.business.core.screening.flow.domain.dos.BloodPressureDataDO
     **/
    @GetMapping("/bloodPressure/{planStudentId}")
    public ApiResult getBloodPressure(@PathVariable Integer planStudentId) {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentId(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId());
        if (Objects.isNull(screeningResult)) {
            return ApiResult.success();
        }
        return ApiResult.success(BloodPressureDTO.getInstance(screeningResult.getBloodPressureData()));
    }


    /**
     * 常见病：疾病史保存
     *
     * @param diseasesHistoryDTO diseasesHistoryDTO
     */
    @PostMapping("/diseasesHistory")
    public ApiResult addDiseasesHistory(@Valid @RequestBody DiseasesHistoryDTO diseasesHistoryDTO) {
        if (diseasesHistoryDTO.isValid()) {
            CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
            visionScreeningBizService.saveOrUpdateStudentScreenData(diseasesHistoryDTO,currentUser.getClientId());
            return ApiResult.success();
        } else {
            return ApiResult.failure("请输入正确的参数");
        }
    }

    /**
     * 获取疾病史数据
     *
     * @param planStudentId 筛查计划学生ID
     * @return com.wupol.myopia.business.core.screening.flow.domain.dos.BloodPressureDataDO
     **/
    @GetMapping("/diseasesHistory/{planStudentId}")
    public ApiResult getDiseasesHistory(@PathVariable Integer planStudentId) {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentId(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId());
        if (Objects.isNull(screeningResult)) {
            return ApiResult.success();
        }
        return ApiResult.success(DiseasesHistoryDTO.getInstance(screeningResult.getDiseasesHistoryData()));
    }


    /**
     * 常见病：个人隐私保存
     *
     * @param privacyDTO privacyDTO
     */
    @PostMapping("/privacy")
    public ApiResult addPrivacy(@Valid @RequestBody PrivacyDTO privacyDTO) {
        if (privacyDTO.isValid()) {
            CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
            visionScreeningBizService.saveOrUpdateStudentScreenData(privacyDTO,currentUser.getClientId());
            return ApiResult.success();
        } else {
            return ApiResult.failure("请输入正确的参数");
        }
    }

    /**
     * 获取个人隐私数据
     *
     * @param planStudentId 筛查计划学生ID
     * @return com.wupol.myopia.business.core.screening.flow.domain.dos.BloodPressureDataDO
     **/
    @GetMapping("/privacy/{planStudentId}")
    public ApiResult getPrivacy(@PathVariable Integer planStudentId) {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentId(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId());
        if (Objects.isNull(screeningResult)) {
            return ApiResult.success();
        }
        return ApiResult.success(PrivacyDTO.getInstance(screeningResult.getPrivacyData()));
    }

    /**
     * 是否可以复测
     *
     * @param planStudentId 筛查计划学生ID
     * @return boolean
     **/
    @GetMapping("/checkRetest/{planStudentId}")
    public boolean checkRetest(@PathVariable Integer planStudentId) {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentId(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId());
        if (Objects.isNull(screeningResult)) {
            return false;
        }
        visionScreeningBizService.verifyScreening(screeningResult, screeningResult.getScreeningType() == 1);
        return true;
    }

    /**
     * 不能检查原因
     *
     * @param planStudentId 筛查计划学生ID
     * @return boolean
     **/
    @PutMapping("/noExamine/{planStudentId}")
    public boolean addNoExamine(@PathVariable Integer planStudentId, @RequestParam(value = "state", defaultValue = "0") Integer state) {
        ScreeningPlanSchoolStudent screeningPlan = screeningPlanSchoolStudentService.findOne(new ScreeningPlanSchoolStudent().setId(planStudentId));
        Assert.notNull(screeningPlan, "不存在筛查计划");
        screeningPlan.setState(state);
        return screeningPlanSchoolStudentService.updateById(screeningPlan);
    }

    /**
     * 筛查不准确说明
     *
     * @param deviationDTO 筛查计划学生ID
     * @return boolean
     **/
    @PostMapping("/inaccurate/{planStudentId}")
    public void addInaccurate(@Valid @RequestBody DeviationDTO deviationDTO, @PathVariable Integer planStudentId) {
        if (deviationDTO.isValid()) {
            VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentId(planStudentId, deviationDTO.getDeptId());
            if (Objects.isNull(screeningResult)) {
                return;
            }
            ScreeningPlan screeningPlan = screeningPlanService.findOne(new ScreeningPlan().setId(screeningResult.getPlanId()));
            visionScreeningBizService.verifyScreening(screeningResult, screeningPlan.getScreeningType() == 1);
            // 只是复测数据
            deviationDTO.setIsState(1);
            CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
            visionScreeningBizService.saveOrUpdateStudentScreenData(deviationDTO,currentUser.getClientId());
        }
    }

    /**
     * 查询班级的学生检查情况
     *
     * @param schoolId 学校名称
     * @param gradeId  年级名称
     * @param classId  班级名称
     * @return com.wupol.myopia.business.api.screening.app.domain.vo.ClassScreeningProgress
     **/
    @GetMapping("/school/findAllStudentNameState")
    public ClassScreeningProgress findClassScreefningStudent(@NotNull(message = "学校ID不能为空") Integer schoolId,
                                                             @NotNull(message = "年级ID不能为空") Integer gradeId,
                                                             @NotNull(message = "班级ID不能为空") Integer classId,
                                                             @RequestParam(value = "channel", defaultValue = "0") Integer channel) {
        return screeningAppService.findClassScreeningStudent(schoolId, gradeId, classId, CurrentUserUtil.getCurrentUser().getOrgId(), channel);
    }

    /**
     * 班级复测情况
     *
     * @param schoolId 学校名称
     * @param gradeId  年级名称
     * @param classId  班级名称
     * @return com.wupol.myopia.business.api.screening.app.domain.vo.ClassScreeningProgress
     **/
    @GetMapping("/school/findClassScreeningStudentState")
    public ClassScreeningProgressState findClassScreeningStudentState(@NotNull(message = "学校ID不能为空") Integer schoolId,
                                                                      @NotNull(message = "年级ID不能为空") Integer gradeId,
                                                                      @NotNull(message = "班级ID不能为空") Integer classId,
                                                                      @RequestParam(value = "channel", defaultValue = "0") Integer channel) {
        return screeningAppService.findClassScreeningStudentState(schoolId, gradeId, classId, CurrentUserUtil.getCurrentUser().getOrgId(), channel);
    }
}
