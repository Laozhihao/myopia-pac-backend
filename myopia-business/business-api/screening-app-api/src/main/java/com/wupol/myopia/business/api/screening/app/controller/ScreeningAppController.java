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
import com.wupol.myopia.business.api.screening.app.domain.vo.*;
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
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentScreeningProgressVO;
import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentVO;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationStaff;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationStaffService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
    private static final Logger logger = LoggerFactory.getLogger(ScreeningAppController.class);

    private static final String ERROR_MSG = "请输入正确的参数";

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
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = screeningPlanSchoolStudentService.getCurrentReleasePlanStudentByOrgIdAndSchoolId(schoolId, CurrentUserUtil.getCurrentUser().getOrgId(), channel);
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
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = screeningPlanSchoolStudentService.getCurrentRelePlanStudentByGradeIdAndScreeningOrgId(gradeId, CurrentUserUtil.getCurrentUser().getOrgId(), channel);
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
                                              @RequestParam(value = "size", defaultValue = "999") Integer size) {
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
        IPage<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentPage = screeningPlanSchoolStudentService.getCurrentReleasePlanScreeningStudentList(screeningStudentQuery, page, size, channel);
        List<ScreeningPlanSchoolStudent> records = screeningPlanSchoolStudentPage.getRecords();
        Map<Integer, SchoolGrade> gradeMap = schoolGradeService.getGradeMapByIds(records.stream().map(ScreeningPlanSchoolStudent::getGradeId).collect(Collectors.toList()));
        Map<Integer, SchoolClass> classMap = schoolClassService.getClassMapByIds(records.stream().map(ScreeningPlanSchoolStudent::getClassId).collect(Collectors.toList()));

        List<StudentVO> studentVOs = records.stream()
                .sorted(Comparator.comparing(ScreeningPlanSchoolStudent::getCreateTime).reversed())
                .map(screeningPlanSchoolStudent -> StudentVO.getInstance(screeningPlanSchoolStudent, gradeMap.getOrDefault(screeningPlanSchoolStudent.getGradeId(), new SchoolGrade()), classMap.getOrDefault(screeningPlanSchoolStudent.getClassId(), new SchoolClass()))).collect(Collectors.toList());
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
        return ApiResult.success(StudentVO.getInstance(screeningPlanSchoolStudent, schoolGradeService.getById(screeningPlanSchoolStudent.getGradeId()), schoolClassService.getById(screeningPlanSchoolStudent.getClassId())));
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
     * 保存汇总的检查数据
     */
    @PostMapping("/eye/addTotalMedicalRecordData")
    public ApiResult addTotalMedicalRecordData(@Valid @RequestBody ScreeningResultDataVO screeningResultDataVO) {
        // 先判断接收到的全部检查数据的合法性，再保存非空的
        if (screeningResultDataVO.getMultiCheckData() != null && !screeningResultDataVO.getMultiCheckData().isValid()) {
            return ApiResult.failure("复合检查数据,请输入正确的参数");
        }

        if (screeningResultDataVO.getComputerOptometryData() != null && !screeningResultDataVO.getComputerOptometryData().isValid()) {
            return ApiResult.failure("验光数据,请输入正确的参数");
        }

        if (screeningResultDataVO.getVisionData() != null && !screeningResultDataVO.getVisionData().isValid()) {
            return ApiResult.failure("视力检查数据,请输入正确的参数");
        }

        if (screeningResultDataVO.getBiometricData() != null && !screeningResultDataVO.getBiometricData().isValid()) {
            return ApiResult.failure("生物测量数据,请输入正确的参数");
        }

        if (screeningResultDataVO.getPupilOptometryData() != null && !screeningResultDataVO.getPupilOptometryData().isValid()) {
            return ApiResult.failure("小瞳验光数据,请输入正确的参数");
        }

        if (screeningResultDataVO.getEyePressureData() != null && !screeningResultDataVO.getEyePressureData().isValid()) {
            return ApiResult.failure("眼压数据,请输入正确的参数");
        }

        if (screeningResultDataVO.getHeightAndWeightData() != null && !screeningResultDataVO.getHeightAndWeightData().isValid()) {
            return ApiResult.failure("身高体重数据,请输入正确的参数");
        }

        if (screeningResultDataVO.getSaprodontiaData() != null && !screeningResultDataVO.getSaprodontiaData().isValid()) {
            return ApiResult.failure("龋齿检查数据,请输入正确的参数");
        }

        if (screeningResultDataVO.getSpineData() != null && !screeningResultDataVO.getSpineData().isValid()) {
            return ApiResult.failure("脊柱检查数据,请输入正确的参数");
        }

        if (screeningResultDataVO.getBloodPressureData() != null && !screeningResultDataVO.getBloodPressureData().isValid()) {
            return ApiResult.failure("血压数据,请输入正确的参数");
        }

        if (screeningResultDataVO.getDiseasesHistoryData() != null && !screeningResultDataVO.getDiseasesHistoryData().isValid()) {
            return ApiResult.failure("疾病史数据,请输入正确的参数");
        }

        if (screeningResultDataVO.getPrivacyData() != null && !screeningResultDataVO.getPrivacyData().isValid()) {
            return ApiResult.failure("个人隐私数据,请输入正确的参数");
        }

        try {
            if (screeningResultDataVO.getMultiCheckData() != null) {
                visionScreeningBizService.saveOrUpdateStudentScreenData(screeningResultDataVO.getMultiCheckData());
            }
            if (screeningResultDataVO.getComputerOptometryData() != null) {
                visionScreeningBizService.saveOrUpdateStudentScreenData(screeningResultDataVO.getComputerOptometryData());
            }
            if (screeningResultDataVO.getVisionData() != null) {
                visionScreeningBizService.saveOrUpdateStudentScreenData(screeningResultDataVO.getVisionData());
            }
            if (screeningResultDataVO.getBiometricData() != null) {
                visionScreeningBizService.saveOrUpdateStudentScreenData(screeningResultDataVO.getBiometricData());
            }
            if (screeningResultDataVO.getPupilOptometryData() != null) {
                visionScreeningBizService.saveOrUpdateStudentScreenData(screeningResultDataVO.getPupilOptometryData());
            }
            if (screeningResultDataVO.getEyePressureData() != null) {
                visionScreeningBizService.saveOrUpdateStudentScreenData(screeningResultDataVO.getEyePressureData());
            }
            if (screeningResultDataVO.getOtherEyeDiseasesData() != null) {
                visionScreeningBizService.saveOrUpdateStudentScreenData(screeningResultDataVO.getOtherEyeDiseasesData());
            }
            if (screeningResultDataVO.getHeightAndWeightData() != null) {
                visionScreeningBizService.saveOrUpdateStudentScreenData(screeningResultDataVO.getHeightAndWeightData());
            }
            if (screeningResultDataVO.getSaprodontiaData() != null) {
                visionScreeningBizService.saveOrUpdateStudentScreenData(screeningResultDataVO.getSaprodontiaData());
            }
            if (screeningResultDataVO.getSpineData() != null) {
                visionScreeningBizService.saveOrUpdateStudentScreenData(screeningResultDataVO.getSpineData());
            }
            if (screeningResultDataVO.getBloodPressureData() != null) {
                visionScreeningBizService.saveOrUpdateStudentScreenData(screeningResultDataVO.getBloodPressureData());
            }
            if (screeningResultDataVO.getDiseasesHistoryData() != null) {
                visionScreeningBizService.saveOrUpdateStudentScreenData(screeningResultDataVO.getDiseasesHistoryData());
            }
            if (screeningResultDataVO.getPrivacyData() != null) {
                visionScreeningBizService.saveOrUpdateStudentScreenData(screeningResultDataVO.getPrivacyData());
            }
            if (screeningResultDataVO.getDeviationData() != null) {
                visionScreeningBizService.saveOrUpdateStudentScreenData(screeningResultDataVO.getDeviationData());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ApiResult.success(e.getMessage());
        }
        return ApiResult.success();
    }

    /**
     * 保存视力筛查
     *
     * @return
     */
    @PostMapping("/eye/addVision")
    public ApiResult addStudentVision(@Valid @RequestBody VisionDataDTO visionDataDTO) {
        if (visionDataDTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(visionDataDTO);
            RetestStudentVO retestStudentVO = screeningAppService.getNumerationSecondCheck(visionDataDTO);
            if (!retestStudentVO.isLeftNakedVision() && !retestStudentVO.isRightNakedVision() && !retestStudentVO.isLeftCorrectedVision() && retestStudentVO.isRightCorrectedVision()) {
                // 清空误差视力检查误差说明
                DeviationDTO deviationDTO = DeviationDTO.getInstance(retestStudentVO.getVisionScreeningResult().getSecond().getDeviationData());
                if(Objects.nonNull(deviationDTO)){
                    deviationDTO.setPlanStudentId(String.valueOf(visionDataDTO.getPlanStudentId()));
                    deviationDTO.setVisionOrOptometryDeviationRemark(null);
                    deviationDTO.setVisionOrOptometryDeviationType(null);
                    addInaccurate(deviationDTO, visionDataDTO.getPlanStudentId());
                }
            }
            return ApiResult.success();
        } else {
            return ApiResult.failure(ERROR_MSG);
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
            visionScreeningBizService.saveOrUpdateStudentScreenData(computerOptometryDTO);
            RetestStudentVO retestStudentVO = screeningAppService.getNumerationSecondCheck(computerOptometryDTO);
            if (!retestStudentVO.isLeftNakedVision() && !retestStudentVO.isRightNakedVision() &&  !retestStudentVO.isLeftCorrectedVision() && !retestStudentVO.isRightCorrectedVision()) {
                // 清空误差视力检查误差说明
                DeviationDTO deviationDTO = DeviationDTO.getInstance(retestStudentVO.getVisionScreeningResult().getSecond().getDeviationData());
                if(Objects.nonNull(deviationDTO)){
                    deviationDTO.setPlanStudentId(String.valueOf(computerOptometryDTO.getPlanStudentId()));
                    deviationDTO.setVisionOrOptometryDeviationRemark(null);
                    deviationDTO.setVisionOrOptometryDeviationType(null);
                    addInaccurate(deviationDTO, computerOptometryDTO.getPlanStudentId());
                }
            }
            return ApiResult.success();
        } else {
            return ApiResult.failure(ERROR_MSG);
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
            visionScreeningBizService.saveOrUpdateStudentScreenData(biometricDataDTO);
            return ApiResult.success();
        } else {
            return ApiResult.failure(ERROR_MSG);
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
    public ApiResult addMultiCheck(@Valid @RequestBody MultiCheckDataDTO multiCheckDataDTO) {
        if (multiCheckDataDTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(multiCheckDataDTO);
            return ApiResult.success();
        } else {
            return ApiResult.failure(ERROR_MSG);
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
            visionScreeningBizService.saveOrUpdateStudentScreenData(pupilOptometryDTO);
            return ApiResult.success();
        } else {
            return ApiResult.failure(ERROR_MSG);
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
            visionScreeningBizService.saveOrUpdateStudentScreenData(eyePressureDataDTO);
            return ApiResult.success();
        } else {
            return ApiResult.failure(ERROR_MSG);
        }
    }

    /**
     * 保存眼位数据
     *
     * @return
     */
    @PostMapping("/eye/addOcularInspection")
    public ApiResult addOcularInspection(@Valid @RequestBody OcularInspectionDataDTO ocularInspectionDataDTO) {
        if (ocularInspectionDataDTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(ocularInspectionDataDTO);
            return ApiResult.success();
        } else {
            return ApiResult.failure(ERROR_MSG);
        }
    }

    /**
     * 保存眼底数据
     *
     * @return
     */
    @PostMapping("/eye/addFundus")
    public ApiResult addFundus(@Valid @RequestBody FundusDataDTO fundusDataDTO) {
        if (fundusDataDTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(fundusDataDTO);
            return ApiResult.success();
        } else {
            return ApiResult.failure(ERROR_MSG);
        }
    }

    /**
     * 保存裂隙灯数据
     *
     * @return
     */
    @PostMapping("/eye/addSlitLamp")
    public ApiResult addSlitLamp(@Valid @RequestBody SlitLampDataDTO slitLampDataDTO) {
        if (slitLampDataDTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(slitLampDataDTO);
            return ApiResult.success();
        } else {
            return ApiResult.failure(ERROR_MSG);
        }
    }

    /**
     * 保存盲及视力损害数据
     *
     * @return
     */
    @PostMapping("/eye/addVisualLossLevel")
    public ApiResult addVisualLossLevel(@Valid @RequestBody VisualLossLevelDataDTO visualLossLevelDataDTO) {
        if (visualLossLevelDataDTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(visualLossLevelDataDTO);
            return ApiResult.success();
        } else {
            return ApiResult.failure(ERROR_MSG);
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
            visionScreeningBizService.saveOrUpdateStudentScreenData(heightAndWeightDataDTO);
            RetestStudentVO retestStudentVO = screeningAppService.getNumerationSecondCheck(heightAndWeightDataDTO);
            if (!retestStudentVO.isHeight() || !retestStudentVO.isWeight()) {
                // 清空误差视力检查误差说明
                DeviationDTO deviationDTO = DeviationDTO.getInstance(retestStudentVO.getVisionScreeningResult().getSecond().getDeviationData());
                if(Objects.nonNull(deviationDTO)){
                    deviationDTO.setPlanStudentId(String.valueOf(heightAndWeightDataDTO.getPlanStudentId()));
                    deviationDTO.setHeightWeightDeviationType(null);
                    deviationDTO.setHeightWeightDeviationRemark(null);
                    addInaccurate(deviationDTO, heightAndWeightDataDTO.getPlanStudentId());
                }
            }
            return ApiResult.success();
        } else {
            return ApiResult.failure(ERROR_MSG);
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
            @RequestParam(value = "channel", defaultValue = "0") Integer channel) {

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
        ScreeningPlan currentPlan = screeningPlanService.getCurrentReleasePlan(CurrentUserUtil.getCurrentUser().getOrgId(), appStudentDTO.getSchoolId().intValue(), channel);
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
        return screeningAppService.getClassScreeningProgress(schoolId, gradeId, classId, CurrentUserUtil.getCurrentUser(), isFilter, isState, channel);
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
        if (screeningPlanStudentBizService.isNotMatchScreeningTime(screeningPlanSchoolStudent)) {
            throw new BusinessException(SysEnum.SYS_STUDENT_SCREENING_TIME_ERROR.getMessage());
        }
        StudentVO studentVO = StudentVO.getInstance(screeningPlanSchoolStudent, schoolGradeService.getById(screeningPlanSchoolStudent.getGradeId()), schoolClassService.getById(screeningPlanSchoolStudent.getClassId()));
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
     * 获取眼位
     *
     * @param planStudentId 筛查计划学生ID
     **/
    @GetMapping("/getOcularInspectionData/{planStudentId}")
    public OcularInspectionDataDTO getOcularInspectionData(@PathVariable Integer planStudentId) {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentId(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId());
        if (Objects.isNull(screeningResult)) {
            return new OcularInspectionDataDTO();
        }
        return OcularInspectionDataDTO.getInstance(screeningResult.getOcularInspectionData());
    }

    /**
     * 获取眼底
     *
     * @param planStudentId 筛查计划学生ID
     **/
    @GetMapping("/getFundusData/{planStudentId}")
    public FundusDataDTO getFundusData(@PathVariable Integer planStudentId) {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentId(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId());
        if (Objects.isNull(screeningResult)) {
            return new FundusDataDTO();
        }
        return FundusDataDTO.getInstance(screeningResult.getFundusData());
    }

    /**
     * 获取裂隙灯
     *
     * @param planStudentId 筛查计划学生ID
     **/
    @GetMapping("/getSlitLampData/{planStudentId}")
    public SlitLampDataDTO getSlitLampData(@PathVariable Integer planStudentId) {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentId(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId());
        if (Objects.isNull(screeningResult)) {
            return new SlitLampDataDTO();
        }
        return SlitLampDataDTO.getInstance(screeningResult.getSlitLampData());
    }

    /**
     * 获取盲及视力损害
     *
     * @param planStudentId 筛查计划学生ID
     **/
    @GetMapping("/getVisualLossLevelData/{planStudentId}")
    public VisualLossLevelDataDTO getVisualLossLevelData(@PathVariable Integer planStudentId) {
        VisionScreeningResult screeningResult = screeningAppService.getVisionScreeningResultByPlanStudentId(planStudentId, CurrentUserUtil.getCurrentUser().getOrgId());
        if (Objects.isNull(screeningResult)) {
            return new VisualLossLevelDataDTO();
        }
        return VisualLossLevelDataDTO.getInstance(screeningResult.getVisualLossLevelData());
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
        Set<Integer> currentPlanIds = screeningPlanService.getCurrentReleasePlanIds(CurrentUserUtil.getCurrentUser().getOrgId(), channel);
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
    public PlanStudentInfoDTO getLatestScreeningStudent(@RequestParam(value = "channel", defaultValue = "0") Integer channel) {
        Set<Integer> currentPlanIds = screeningPlanService.getCurrentReleasePlanIds(CurrentUserUtil.getCurrentUser().getOrgId(), channel);
        if (CollectionUtils.isEmpty(currentPlanIds)) {
            return new PlanStudentInfoDTO();
        }

        VisionScreeningResult visionScreeningResult = visionScreeningResultService.getOneByPlanIdsOrderByUpdateTimeDesc(currentPlanIds);
        if (Objects.isNull(visionScreeningResult)) {
            List<ScreeningPlanSchool> schoolPlan = screeningPlanSchoolService.getSchoolListsByPlanId(Lists.newArrayList(currentPlanIds).get(0));
            if (Objects.nonNull(schoolPlan) && !schoolPlan.isEmpty()) {
                ScreeningPlanSchoolStudent planStudent = screeningPlanSchoolStudentService.getOneByPlanId(Lists.newArrayList(currentPlanIds).get(0));
                if (Objects.nonNull(planStudent)) {
                    planStudent.setSchoolName(schoolService.getById(planStudent.getSchoolId()).getName())
                            .setGradeId(planStudent.getGradeId())
                            .setSchoolId(planStudent.getSchoolId())
                            .setClassId(planStudent.getClassId());
                    PlanStudentInfoDTO infoDTO = new PlanStudentInfoDTO();
                    BeanUtils.copyProperties(planStudent, infoDTO);
                    infoDTO.setGradeName(schoolGradeService.getById(planStudent.getGradeId()).getName())
                            .setClassName(schoolClassService.getById(planStudent.getClassId()).getName());
                    return infoDTO;
                } else {
                    PlanStudentInfoDTO planStudentInfoDTO = new PlanStudentInfoDTO();
                    planStudentInfoDTO.setSchoolId(schoolPlan.get(0).getSchoolId())
                            .setSchoolName(schoolService.getById(schoolPlan.get(0).getSchoolId()).getName());
                    return planStudentInfoDTO;
                }
            } else {
                return new PlanStudentInfoDTO();
            }
        }
        ScreeningPlanSchoolStudent planStudent = screeningPlanSchoolStudentService.getById(visionScreeningResult.getScreeningPlanSchoolStudentId());
        planStudent.setSchoolName(schoolService.getById(planStudent.getSchoolId()).getName())
                .setGradeId(planStudent.getGradeId())
                .setSchoolId(planStudent.getSchoolId())
                .setClassId(planStudent.getClassId());
        PlanStudentInfoDTO infoDTO = new PlanStudentInfoDTO();
        BeanUtils.copyProperties(planStudent, infoDTO);
        return infoDTO.setGradeName(schoolGradeService.getById(planStudent.getGradeId()).getName())
                .setClassName(schoolClassService.getById(planStudent.getClassId()).getName());
    }

    /**
     * 更新筛查学生信息
     *
     * @param requestDTO 更新信息
     * @return void
     **/
    @PostMapping("/update/planStudent")
    public void updatePlanStudent(@RequestBody @Valid UpdatePlanStudentRequestDTO requestDTO) {
        // 如果护照、身份证都为空，则直接更新筛查学生
        if (StringUtils.isAllBlank(requestDTO.getIdCard(), requestDTO.getPassport())) {
            screeningPlanStudentBizService.updateAppPlanStudent(requestDTO);
            return;
        }
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
            visionScreeningBizService.saveOrUpdateStudentScreenData(saprodontiaDTO);
            return ApiResult.success();
        } else {
            return ApiResult.failure(ERROR_MSG);
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
            visionScreeningBizService.saveOrUpdateStudentScreenData(spineDTO);
            return ApiResult.success();
        } else {
            return ApiResult.failure(ERROR_MSG);
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
            visionScreeningBizService.saveOrUpdateStudentScreenData(bloodPressureDTO);
            return ApiResult.success();
        } else {
            return ApiResult.failure(ERROR_MSG);
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
            visionScreeningBizService.saveOrUpdateStudentScreenData(diseasesHistoryDTO);
            return ApiResult.success();
        } else {
            return ApiResult.failure(ERROR_MSG);
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
            visionScreeningBizService.saveOrUpdateStudentScreenData(privacyDTO);
            return ApiResult.success();
        } else {
            return ApiResult.failure(ERROR_MSG);
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
            visionScreeningBizService.saveOrUpdateStudentScreenData(deviationDTO);
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

    /**
     * 获取筛查人员类型
     *
     * @return true-自动生成的筛查人员 false-普通筛查人员
     */
    @GetMapping("/check/staffType")
    public Boolean checkStaffType() {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        if (currentUser.isSchoolScreeningUser()) {
            return false;
        }
        ScreeningOrganizationStaff staff = screeningOrganizationStaffService.getStaffsByUserId(currentUser.getId());
        if (Objects.isNull(staff)) {
            throw new BusinessException("筛查人员信息异常");
        }
        return ScreeningOrganizationStaff.AUTO_CREATE_SCREENING_PERSONNEL == staff.getType();
    }

    /**
     * 查询某个筛查机构下的学校的学生
     *
     * @param schoolId 学校Id
     *
     * @return List<ScreeningPlanSchoolStudent>
     */
    @GetMapping("/school/planStudentList")
    public List<ScreeningPlanSchoolStudent> schoolPlanStudentList(@NotNull(message = "schoolId不能为空") Integer schoolId, @NotNull(message = "渠道不能为空") Integer channel) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return screeningPlanBizService.getPlanSchoolStudent(currentUser.getOrgId(), schoolId, channel);
    }

    /**
     * 查询学校的班级
     *
     * @param schoolId 学校Id
     *
     * @return List<SchoolClass>
     */
    @GetMapping("/school/getClassBySchoolId")
    public List<SchoolClass> getClassBySchoolId(@NotNull(message = "schoolId不能为空") Integer schoolId) {
        return schoolClassService.listBySchoolId(schoolId);
    }
}
