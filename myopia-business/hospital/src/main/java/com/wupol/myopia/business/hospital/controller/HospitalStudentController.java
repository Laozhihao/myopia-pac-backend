package com.wupol.myopia.business.hospital.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.hospital.domain.model.Consultation;
import com.wupol.myopia.business.hospital.service.ConsultationService;
import com.wupol.myopia.business.hospital.service.HospitalStudentService;
import com.wupol.myopia.business.management.domain.dto.HospitalStudentDTO;
import com.wupol.myopia.business.management.domain.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

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


    @GetMapping()
    public HospitalStudentDTO getStudent(String token, String idCard) {
        return hospitalStudentService.getStudent(token, idCard);
    }

    @GetMapping("/{id}")
    public HospitalStudentDTO getStudent(@PathVariable("id") Integer id) {
        return hospitalStudentService.getStudentById(id);
    }

    @GetMapping("/recentList")
    public List<Student> getRecentList() throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return hospitalStudentService.getRecentList(user.getOrgId());
    }

    @GetMapping("/list")
    public List<HospitalStudentDTO> getStudentList(String nameLike) throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return hospitalStudentService.getStudentList(user.getOrgId(), nameLike);
    }

    @PostMapping()
    public Integer saveStudent(@RequestBody @Valid HospitalStudentDTO studentVo) throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return hospitalStudentService.saveStudent(studentVo, user.getOrgId());
    }


}