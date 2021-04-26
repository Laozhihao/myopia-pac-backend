package com.wupol.myopia.business.api.screening.app.controller;

import cn.hutool.core.util.IdcardUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.api.screening.app.domain.dto.AppStudentDTO;
import com.wupol.myopia.business.api.screening.app.domain.vo.EyeDiseaseVO;
import com.wupol.myopia.business.api.screening.app.domain.vo.RescreeningResultVO;
import com.wupol.myopia.business.api.screening.app.domain.vo.StudentVO;
import com.wupol.myopia.business.api.screening.app.enums.ErrorEnum;
import com.wupol.myopia.business.api.screening.app.enums.StudentExcelEnum;
import com.wupol.myopia.business.api.screening.app.enums.SysEnum;
import com.wupol.myopia.business.api.screening.app.service.ScreeningAppService;
import com.wupol.myopia.business.api.screening.app.utils.CommUtil;
import com.wupol.myopia.business.common.utils.constant.EyeDiseasesEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultSearchDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.screening.others.SysStudent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 筛查端App接口
 *
 * @Author Chikong
 * @Date 2021-01-21
 */
@CrossOrigin
@RestController
@RequestMapping("/app/screening")
@Slf4j
public class ScreeningAppController {

    @Autowired
    private ScreeningAppService screeningAppService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private ScreeningPlanSchoolService screeningPlanSchoolService;
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


    /**
     * 模糊查询某个筛查机构下的学校的
     *
     * @param schoolName 模糊查询
     * @param deptId     机构id
     * @param isReview   是否复测
     * @return
     */
    @GetMapping("/school/findAllLikeSchoolName")
    public ApiResult getSchoolNameByNameLike(@RequestParam String schoolName, String deptId, Boolean isReview) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        List<School> schools = screeningPlanSchoolService.getSchoolByOrgId(schoolName, currentUser.getOrgId());
        return ApiResult.success(schools.stream().map(School::getName).collect(Collectors.toSet()));
    }

    /**
     * 查询学校的年级名称
     *
     * @param schoolName 学校名
     * @param deptId     机构id
     * @return
     */
    @GetMapping("/school/findAllGradeNameBySchoolName")
    public ApiResult getGradeNameBySchoolName(@RequestParam String schoolName, @RequestParam Integer deptId, boolean all) {
        Set<String> gradeNameSet;
        if (all) {
            //查找全部的年级
            List<SchoolGrade> schoolGrades = schoolGradeService.getBySchoolName(schoolName);
            gradeNameSet = schoolGrades.stream().map(SchoolGrade::getName).collect(Collectors.toSet());
        } else {
            List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = screeningPlanSchoolStudentService.getSchoolByOrgIdAndSchoolName(schoolName, deptId);
            if (CollectionUtils.isEmpty(screeningPlanSchoolStudents)) {
                return ApiResult.success();
            }
            gradeNameSet = screeningPlanSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getGradeName).collect(Collectors.toSet());
        }
        return ApiResult.success(gradeNameSet);
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
    public ApiResult getClassNameBySchoolNameAndGradeName(String schoolName, String gradeName, Integer deptId, boolean all) {
        Set<String> classNameSet = new HashSet<>();
        if (all) {
            List<SchoolClass> schoolClassList = schoolClassService.getBySchoolNameAndGradeName(schoolName, gradeName);
            classNameSet = schoolClassList.stream().map(SchoolClass::getName).collect(Collectors.toSet());
        } else {
            List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = screeningPlanSchoolStudentService.getClassNameBySchoolNameAndGradeName(schoolName, gradeName, deptId);
            classNameSet = screeningPlanSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getClassName).collect(Collectors.toSet());
        }
        return ApiResult.success(classNameSet);
    }

    /**
     * 获取学生的信息：
     *
     * @param
     * @return
     */
    @GetMapping("/student/findOneById")
    public ApiResult getStudentById(String studentId, String planStudentId, @RequestParam Integer deptId) {
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = new ScreeningPlanSchoolStudent();
        if (StringUtils.isNotBlank(planStudentId)) {
            screeningPlanSchoolStudent.setId(Integer.valueOf(planStudentId));
        } else {
            screeningPlanSchoolStudent.setStudentId(Integer.valueOf(studentId)).setScreeningOrgId(deptId);
        }
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = screeningPlanSchoolStudentService.getByEntity(screeningPlanSchoolStudent);
        if (CollectionUtils.isEmpty(screeningPlanSchoolStudents)) {
            return ApiResult.failure(SysEnum.SYS_STUDENT_NULL.getCode(), SysEnum.SYS_STUDENT_NULL.getMessage());
        }
        return ApiResult.success(StudentVO.getInstance(screeningPlanSchoolStudents.get(0)));
    }

    /**
     * 获取筛查就机构对应的学校
     *
     * @param deptId 机构id
     * @return
     */
    @GetMapping("/findSchoolByDeptId")
    public ApiResult listSchoolByScreeningOrgId(Integer deptId) {
        //筛查机构未完成的学校的信息
        return ApiResult.success(screeningAppService.getSchoolByScreeningOrgId(deptId));
    }

    /**
     * 查询眼睛疾病
     *
     * @return
     */
    @PostMapping("/eye/findAllEyeDisease")
    public ApiResult getAllEyeDisease() {
        List<String> eyeDiseaseList = EyeDiseasesEnum.eyeDiseaseList;
        List<EyeDiseaseVO> LeftEyeDiseaseVO = eyeDiseaseList.stream().map(eyeDiseas -> {
            EyeDiseaseVO eyeDiseaseVO = new EyeDiseaseVO();
            eyeDiseaseVO.setEye("L");
            eyeDiseaseVO.setName(eyeDiseas);
            eyeDiseaseVO.setCreateTime(new Date());
            eyeDiseaseVO.setId("1");
            return eyeDiseaseVO;
        }).collect(Collectors.toList());

        List<EyeDiseaseVO> rightEyeDiseaseVO = eyeDiseaseList.stream().map(eyeDiseas -> {
            EyeDiseaseVO eyeDiseaseVO = new EyeDiseaseVO();
            eyeDiseaseVO.setEye("R");
            eyeDiseaseVO.setName(eyeDiseas);
            eyeDiseaseVO.setCreateTime(new Date());
            eyeDiseaseVO.setId("1");
            return eyeDiseaseVO;
        }).collect(Collectors.toList());
        List<EyeDiseaseVO> allEyeDiseaseVos = new ArrayList<>();
        allEyeDiseaseVos.addAll(rightEyeDiseaseVO);
        allEyeDiseaseVos.addAll(LeftEyeDiseaseVO);
        return ApiResult.success(allEyeDiseaseVos);
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
    public @ResponseBody
    ApiResult uploadUserAutographImageWithUser(@RequestParam(value = "deptId") Long deptId,
                                              @RequestParam(value = "userId") Long userId,
                                              @RequestParam(value = "file") MultipartFile file) {
        String imgUrl = screeningAppService.uploadSignPic(CurrentUserUtil.getCurrentUser(), file);
        return ApiResult.success(imgUrl);
    }

    /**
     * 获取用户的基本信息
     *
     * @return
     */
    @GetMapping("/getUserInfo")
    public ApiResult getUserInfo() throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return ApiResult.success(screeningAppService.getUserInfoByUser(currentUser));
    }

    /**
     * 人脸识别 暂时不做
     *
     * @return
     */
    @PostMapping("/recognitionFace")
    public ApiResult recognitionFace(Integer deptId, MultipartFile file) {
        return ApiResult.success(screeningAppService.recognitionFace(deptId, file));
    }


    /**
     * 保存视力筛查
     *
     * @return
     */
    @PostMapping(value = {"/eye/addVision"})
    public ApiResult addStudentVision(@Valid @RequestBody VisionDataDTO visionDataDTO) throws IOException {
        screeningAppService.saveOrUpdateStudentScreenData(visionDataDTO);
        return ApiResult.success();
    }

    /**
     * 保存电脑验光
     *
     * @return
     */
    @PostMapping("/eye/addComputer")
    public ApiResult addStudentComputer(@Valid @RequestBody ComputerOptometryDTO computerOptometryDTO) throws IOException {
        screeningAppService.saveOrUpdateStudentScreenData(computerOptometryDTO);
        return ApiResult.success();
    }

    /**
     * 保存生物测量数据
     *
     * @return
     */
    @PostMapping("/eye/addBiology")
    public ApiResult addStudentBiology(@Valid @RequestBody BiometricDataDTO biometricDataDTO) throws IOException {
        screeningAppService.saveOrUpdateStudentScreenData(biometricDataDTO);
        return ApiResult.success();
    }

    /**
     * 增加该学生的眼睛疾病
     *
     * @return
     */
    @PostMapping("/eye/addEyeDisease")
    public ApiResult addEyeDisease(@Valid @RequestBody OtherEyeDiseasesDTO otherEyeDiseasesDTO) throws IOException {
        screeningAppService.saveOrUpdateStudentScreenData(otherEyeDiseasesDTO);
        return ApiResult.success();
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
    public ApiResult findAllStudentName(
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
                .setScreeningOrgId(deptId.intValue())
                .setSchoolName(schoolName)
                .setClassName(clazzName)
                .setStudentName(studentName)
                .setGradeName(gradeName);
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = screeningPlanSchoolStudentService.listByEntityDescByCreateTime(screeningPlanSchoolStudent, page, size);

        List<StudentVO> studentVOs = screeningPlanSchoolStudents.stream().map(x -> StudentVO.getInstance(x)).collect(Collectors.toList());
        Page<StudentVO> sysStudents = new PageImpl(studentVOs, pageable, studentVOs.size());
        return ApiResult.success(sysStudents);//screeningAppService.getStudentBySchoolNameAndGradeNameAndClassName(pageRequest, schoolId, schoolName, gradeName, clazzName, studentName, deptId, isReview);
    }

    /**
     * 随机获取学生复测质量控制
     *
     * @param
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = "/student/findReviewRandom")
    public @ResponseBody
    ApiResult findAllNameReview(
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
        List<SysStudent> sysStudentList = screeningAppService.getStudentReview(schoolId, gradeName, clazzName, deptId, studentName, current, size, isRandom);
        return ApiResult.success(sysStudentList);
    }

    /**
     * 随机获取学生复测质量控制
     *
     * @param
     * @return
     */
/*    @RequestMapping(method = RequestMethod.GET, path = "/student/findReviewRandom")
    public @ResponseBody
    ResultVO findAllNameReview(
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
        List<SysStudent> sysStudentList = screeningAppService.getStudentReview(schoolId, gradeName, clazzName, deptId, studentName, current, size);
        if (isRandom) {
            sysStudentList = screeningAppService.getRandomData(sysStudentList);
        }
        return ApiResult.success(sysStudentList);
    }*/

    /**
     * 更新复测质控结果 TODO
     *
     * @return
     */
    @PostMapping("/eye/updateReviewResult")
    public ApiResult updateReviewResult(Integer eyeId) {
        return ApiResult.success(screeningAppService.updateReviewResult(eyeId));
    }

    /**
     * 保存学生信息  TODO
     *
     * @return
     */
    @PostMapping("/student/save")
    public ApiResult saveStudent(@RequestBody AppStudentDTO appStudentDTO) throws ParseException {
        appStudentDTO.setDeptId(CurrentUserUtil.getCurrentUser().getOrgId());
        ApiResult apiResult = this.validStudentParam(appStudentDTO);
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
            return ApiResult.failure(ErrorEnum.UNKNOWN_ERROR);
        }
        screeningPlanSchoolStudentService.insertWithStudent(CurrentUserUtil.getCurrentUser(), student, appStudentDTO.getGrade(), appStudentDTO.getClazz(), appStudentDTO.getSchoolName(), school.getSchoolNo(), school.getDistrictId(), appStudentDTO.getSchoolId().intValue(), currentPlan);
        return ApiResult.success();
    }


    /**
     * 搜索复测质控结果
     *
     * @return
     */
    @GetMapping("/eye/findAllReviewResult")
    public ApiResult findAllReviewResult(
            @RequestParam Integer deptId,
            @RequestParam(value = "schoolId") Integer schoolId,
            @RequestParam(value = "gradeName", required = false) String gradeName,
            @RequestParam(value = "clazzName", required = false) String clazzName) {
        ScreeningResultSearchDTO screeningResultSearchDTO = new ScreeningResultSearchDTO();
        screeningResultSearchDTO.setClazzName(clazzName);
        screeningResultSearchDTO.setGradeName(gradeName).setSchoolId(schoolId).setDepId(deptId);
        List<RescreeningResultVO> allReviewResult = screeningAppService.getAllReviewResult(screeningResultSearchDTO);
        //进行数据的转换
        //this.convertToTheOldStructure(allReviewResult);
        // String s = "{\"number\":100,\"qualified\":0,\"gradeName\":\"高一\",\"qualifiedCount\":0,\"eyeResult\":1,\"eyesCount\":1,\"reviewsCount\":1,\"schoolName\":\"吕梁高级中学\",\"content\":[{\"studentSex（性别）\":\"男\",\"lsl\":{\"firstRight\":\"4.7\",\"firstLeft\":\"4.7\",\"reviewRight\":\"4.6\",\"reviewLeft\":\"4.6\",\"qualified\":0,\"leftQualified\":1,\"rightQualified\":1},\"reviewDoctor\":\"张艳珍\",\"studentSchool（学校）\":\"吕梁高级中学\",\"firstDoctor\":\"张艳珍\",\"sph\":{\"firstRight\":\"4.7\",\"firstLeft\":\"4.7\",\"qualified\":0,\"reviewRight\":\"4.6\",\"reviewLeft\":\"4.6\",\"leftQualified\":1,\"rightQualified\":0},\"firstTime\":\"2020-08-19T06:21:18.000+0000\",\"reviewsId\":\"1597819899142384177\",\"cyl\":{\"firstRight\":\"4.7\",\"firstLeft\":\"4.7\",\"qualified\":0,\"reviewRight\":\"4.6\",\"reviewLeft\":\"4.6\",\"leftQualified\":1,\"rightQualified\":1},\"studentGrade（年级）\":\"高一\",\"studentClazz（班级）\":\"1922\",\"studentName（姓名）\":\"常耀方\",\"jzsl\":{\"firstRight\":\"4.7\",\"firstLeft\":\"4.7\",\"reviewRight\":\"4.6\",\"reviewLeft\":\"4.6\",\"qualified\":0,\"leftQualified\":1,\"rightQualified\":1},\"reviewTime\":\"2020-08-19T06:51:39.000+0000\"}],\"clazzName\":1922}";
        // Object allReviewResult = JSON.parse(s);
        ApiResult success = ApiResult.success(allReviewResult);
        System.out.println(success);
        return success;
    }


    /**
     * 校验学生数据的有效性
     *
     * @param appStudentDTO
     * @return
     */
    private ApiResult validStudentParam(AppStudentDTO appStudentDTO) {
        //验证学生生日格式
        if (StringUtils.isNotBlank(appStudentDTO.getBirthday())) {
            String validDate = DateUtil.isValidDate(appStudentDTO.getBirthday());
            if (validDate == null) {
                return ApiResult.failure(ErrorEnum.SYS_STUDENT_BIRTHDAY_FORMAT_ERROR.getCode(), ErrorEnum.SYS_STUDENT_BIRTHDAY_FORMAT_ERROR.getMessage());
            } else {
                appStudentDTO.setBirthday(validDate);
            }
        }
        if (appStudentDTO.getSchoolId() == null || appStudentDTO.getSchoolId() == 0) {
            return ApiResult.failure(ErrorEnum.SYS_STUDENT_SCHOOL_NULL.getCode(), ErrorEnum.SYS_STUDENT_SCHOOL_NULL.getMessage());
        }
        //验证身份号
        if (StringUtils.isNotBlank(appStudentDTO.getIdCard())) {
            boolean flag = IdcardUtil.isValidCard(appStudentDTO.getIdCard());
            if (!flag) {
                return ApiResult.failure(StudentExcelEnum.EXCEL_IDCARD_ERROR.getCode(), StudentExcelEnum.EXCEL_IDCARD_ERROR.getMessage());
            }
        }

        //验证手机号
        if (StringUtils.isNotBlank(appStudentDTO.getStudentPhone())) {
            boolean flag = CommUtil.isMobileNO(appStudentDTO.getStudentPhone());
            if (!flag) {
                //验证是否为电话号
                boolean isPhone = CommUtil.isPhoneNO(appStudentDTO.getStudentPhone());
                if (!isPhone) {
                    return ApiResult.failure(StudentExcelEnum.EXCEL_PHONE_ERROR.getCode(), StudentExcelEnum.EXCEL_PHONE_ERROR.getMessage());
                }
            }
        }
        //设置出生日期
        if (StringUtils.isBlank(appStudentDTO.getBirthday())) {
            if (StringUtils.isNotBlank(appStudentDTO.getIdCard())) {
                appStudentDTO.setBirthday(CommUtil.getBirthday(appStudentDTO.getIdCard()));
            }
        }
        return null;
    }

}
