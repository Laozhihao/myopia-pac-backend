package com.wupol.myopia.business.screening.controller;

import com.myopia.common.constant.EyeDiseasesEnum;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.domain.dto.BiometricDataDTO;
import com.wupol.myopia.business.management.domain.dto.ComputerOptometryDTO;
import com.wupol.myopia.business.management.domain.dto.OtherEyeDiseases;
import com.wupol.myopia.business.management.domain.dto.VisionDataDTO;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.domain.query.PageRequest;
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
public class LLFScreeningAppController {

    @Autowired
    private ScreeningAppService screeningAppService;

    /**
     * 随机获取学生复测信息 TODO jacob
     *
     * @param pageRequest 分页
     * @param deptId      筛查机构id
     * @param schoolId    学校id
     * @param schoolName  学校名称
     * @param gradeName   年级名称
     * @param clazzName   班级名称
     * @return
     */
    @GetMapping("/student/findStudentReviewRandom")
    public List<Student> getStudentReviewWithRandom(PageRequest pageRequest, Integer schoolId, String schoolName, String gradeName, String clazzName, Integer deptId) {
        return screeningAppService.getStudentReviewWithRandom(pageRequest, schoolId, schoolName, gradeName, clazzName, deptId);
    }

    /**
     * 搜索复测质控结果 TODO jacobing
     *
     * @return
     */
    @PostMapping("/eye/findAllReviewResult")
    public List<Object> getAllReviewResult(Integer deptId, String gradeName, String clazzName, Integer schoolId) {
        return screeningAppService.getAllReviewResult(deptId, gradeName, clazzName, schoolId);
    }

    /**
     * 更新复测质控结果 TODO jacob
     *
     * @return
     */
    @PostMapping("/eye/updateReviewResult")
    public Boolean updateReviewResult(Integer eyeId) {
        //TODO 管理端，待修改接收的参数
        return screeningAppService.updateReviewResult(eyeId);
    }

    /**
     * 上传筛查机构用户的签名图片 TODO jacob 暂时无对象存储
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
