package com.wupol.myopia.business.hospital.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.hospital.service.HospitalStudentService;
import com.wupol.myopia.business.management.domain.dto.HospitalStudentDTO;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * 医院的学生管理的App接口
 * @author Chikong
 * @date 2021-02-10
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/hospital/app/student")
public class HospitalStudentController {

    @Autowired
    private HospitalStudentService hospitalStudentService;
    @Autowired
    private StudentService studentService;


    @GetMapping()
    public HospitalStudentDTO getStudent(Integer id, String idCard, String name) {
        return hospitalStudentService.getStudent(id, idCard, name);
    }

    @GetMapping("/{id}")
    public HospitalStudentDTO getStudent(@PathVariable("id") Integer id) {
        return hospitalStudentService.getStudentById(id);
    }

    @GetMapping("/recentList")
    public List<HospitalStudentDTO> getRecentList() throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return hospitalStudentService.getRecentList(user.getOrgId());
    }

    @GetMapping("/list")
    public List<HospitalStudentDTO> getStudentList(String nameLike) throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return hospitalStudentService.getStudentList(user.getOrgId(), nameLike);
    }

    @PostMapping()
    public ApiResult<String> saveStudentArchive(@RequestBody @Valid HospitalStudentDTO studentVo) throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        Integer hospitalId = user.getOrgId();

        Student student = studentService.getByIdCard(studentVo.getIdCard());
        if (Objects.nonNull(student) && hospitalStudentService.existHospitalAndStudentRelationship(hospitalId, student.getId())) {
            return ApiResult.failure("该学生已建档，请勿重复建档");
        }
        Integer studentId = hospitalStudentService.saveStudent(studentVo);
        hospitalStudentService.saveHospitalStudentArchive(hospitalId, studentId);
        return ApiResult.success("建档成功");
    }

    @PutMapping()
    public ApiResult<String> updateStudent(@RequestBody @Valid HospitalStudentDTO studentVo) throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        Integer hospitalId = user.getOrgId();
        // 如果医院没有该学生的档案,则不允许操作
        if (!hospitalStudentService.existHospitalAndStudentRelationship(hospitalId, studentVo.getId())) {
            return ApiResult.failure("该学生未建档");
        }
        hospitalStudentService.saveStudent(studentVo);
        return ApiResult.success("建档成功");
    }


}
