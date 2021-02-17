package com.wupol.myopia.business.screening.controller;

import com.myopia.common.constant.EyeDiseasesEnum;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.domain.dto.*;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.screening.domain.vo.RescreeningResultVO;
import com.wupol.myopia.business.screening.service.ScreeningAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

/**
 * 筛查端App接口
 *
 * @Author Chikong
 * @Date 2021-01-21
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/app/screening")
public class ScreeningAppController {

    @Autowired
    private ScreeningAppService screeningAppService;

    /**
     * 模糊查询所有学校名称
     *
     * @param schoolName 模糊查询
     * @param deptId     机构id
     * @param isReview   是否复测
     * @return
     */
    @GetMapping("/school/findAllLikeSchoolName")
    public List<String> getSchoolNameByNameLike(@RequestParam String schoolName, @RequestParam Integer deptId, Boolean isReview) {
        return screeningAppService.getSchoolNameBySchoolNameLike(schoolName, deptId, isReview);
    }

    /**
     * 查询学校的年级名称
     *
     * @param schoolName 学校名
     * @param deptId     机构id
     * @return
     */
    @GetMapping("/school/findAllGradeNameBySchoolName")
    public List<String> getGradeNameBySchoolName(@RequestParam String schoolName, @RequestParam Integer deptId) {
        return screeningAppService.getGradeNameBySchoolName(schoolName, deptId);
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
    public List<String> getClassNameBySchoolNameAndGradeName(String schoolName, String gradeName, Integer deptId) {
        deptId = 1;
        return screeningAppService.getClassNameBySchoolNameAndGradeName(schoolName, gradeName, deptId);
    }

    /**
     * 获取学校年级班级对应的学生名称
     *
     * @param schoolId    学校id, 仅复测时有
     * @param schoolName  学校名称
     * @param gradeName   年级名称
     * @param clazzName   班级名称
     * @param studentName 学生名称
     * @param deptId      机构id
     * @param isReview    是否复测
     * @return
     */
    @GetMapping("/school/findAllStudentName")
    public List<Student> getStudentNameBySchoolNameAndGradeNameAndClassName(PageRequest pageRequest, Integer schoolId, String schoolName, String gradeName, String clazzName, String studentName, Integer deptId, Boolean isReview) {
        deptId = 1;
        return screeningAppService.getStudentBySchoolNameAndGradeNameAndClassName(pageRequest, schoolId, schoolName, gradeName, clazzName, studentName, deptId, isReview);
    }
    /**
     * 获取学生
     *
     * @param id 学生id
     * @return
     */
    @GetMapping("/student/findOneById")
    public Student getStudentById(Integer id) {
        return screeningAppService.getStudentById(id);
    }


    /**
     * 保存学生信息
     *
     * @return
     */
    @PostMapping("/student/save")
    public Object saveStudent(Student student) {
        //TODO 管理端，待查询再修改
        return screeningAppService.saveStudent(student);
    }

    /**
     * 人脸识别
     *
     * @return
     */
    @PostMapping("/recognitionFace")
    public Object recognitionFace(Integer deptId, MultipartFile file) {
        //TODO 筛查端，待修改
        return screeningAppService.recognitionFace(deptId, file);
    }



    //分割线----------------------

    /**
     * 随机获取学生复测信息 TODO jacob 要理解一下
     *
     * @param screeningResultSearchDTO
     * @return
     */
    @GetMapping("/student/findStudentReviewRandom")
    public List<Student> getStudentReviewWithRandom(@Valid @RequestBody ScreeningResultSearchDTO screeningResultSearchDTO) {
        return null; //screeningAppService.getStudentReviewWithRandom(screeningResultSearchDTO);
    }

    /**
     * 搜索复测质控结果
     *
     * @return
     */
    @PostMapping("/eye/findAllReviewResult")
    public List<RescreeningResultVO> getAllReviewResult(@Valid @RequestBody ScreeningResultSearchDTO screeningResultSearchDTO) {
        List<RescreeningResultVO> allReviewResult = screeningAppService.getAllReviewResult(screeningResultSearchDTO);
        return allReviewResult;
    }

    /**
     * 更新复测质控结果 TODO
     *
     * @return
     */
    @PostMapping("/eye/updateReviewResult")
    public Boolean updateReviewResult(Integer eyeId) {
        //TODO 管理端，待修改接收的参数
        return screeningAppService.updateReviewResult(eyeId);
    }

    /**
     * 上传筛查机构用户的签名图片 TODO jacob 暂时无对象存储 年后
     *
     * @param deptId 机构id
     * @param userId 用户id
     * @param file   签名
     * @return
     */
    @PostMapping("/user/uploadSignPic")
    public Boolean uploadSignPic(Integer deptId, Integer userId, MultipartFile file) {
        return screeningAppService.uploadSignPic(deptId, userId, file);
    }


    /**
     * 获取筛查就机构对应的学校
     *
     * @param deptId 机构id
     * @return
     */
    @GetMapping("/findSchoolByDeptId")
    public List<School> getSchoolByScreeningOrgId(Integer deptId) {
        //筛查机构未完成的学校的信息
        return screeningAppService.getSchoolByScreeningOrgId(deptId);
    }

    /**
     * 查询眼睛疾病
     *
     * @return
     */
    @PostMapping("/eye/findAllEyeDisease")
    public List<String> getAllEyeDisease() {
        return EyeDiseasesEnum.eyeDiseaseList;
    }


    /**
     * 保存视力筛查
     *
     * @return
     */
    @PostMapping(value = {"/eye/addVision"})
    public void addStudentVision(@Valid @RequestBody VisionDataDTO visionDataDTO) throws IOException {
        screeningAppService.saveOrUpdateStudentScreenData(visionDataDTO);
    }

    /**
     * 保存电脑验光
     *
     * @return
     */
    @PostMapping("/eye/addComputer")
    public void addStudentComputer(@Valid @RequestBody ComputerOptometryDTO computerOptometryDTO) throws IOException {
        screeningAppService.saveOrUpdateStudentScreenData(computerOptometryDTO);
    }

    /**
     * 保存生物测量数据
     *
     * @return
     */
    @PostMapping("/eye/addBiology")
    public void addStudentBiology(@Valid @RequestBody BiometricDataDTO biometricDataDTO) throws IOException {
        screeningAppService.saveOrUpdateStudentScreenData(biometricDataDTO);
    }

    /**
     * 增加该学生的眼睛疾病
     *
     * @return
     */
    @PostMapping("/eye/addEyeDisease")
    public void addEyeDisease(@Valid @RequestBody OtherEyeDiseases otherEyeDiseases) throws IOException {
        screeningAppService.saveOrUpdateStudentScreenData(otherEyeDiseases);
    }
}
