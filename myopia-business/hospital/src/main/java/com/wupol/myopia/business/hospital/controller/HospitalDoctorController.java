package com.wupol.myopia.business.hospital.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.hospital.domain.model.Doctor;
import com.wupol.myopia.business.hospital.domain.vo.DoctorVo;
import com.wupol.myopia.business.hospital.service.HospitalDoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

/**
 * 医院的医生管理的App接口
 * @author Chikong
 * @date 2021-02-10
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/hospital/app/doctor")
public class HospitalDoctorController {

    @Autowired
    private HospitalDoctorService hospitalDoctorService;


    @GetMapping("/list")
    public List<DoctorVo> getDoctorList(String like) throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return hospitalDoctorService.getDoctorVoList(user.getOrgId(), like);
    }

    @GetMapping("/{id}")
    public DoctorVo getDoctor(@PathVariable("id") Integer id) throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return hospitalDoctorService.getDoctor(user.getOrgId(), id);
    }

    @PostMapping()
    public Boolean saveDoctor(@RequestBody @Valid Doctor doctor) throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        hospitalDoctorService.saveDoctor(user, doctor);
        return true;
    }

    @DeleteMapping("/{id}")
    public Boolean deleteDoctor(@PathVariable("id") Integer id) throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        hospitalDoctorService.deleteDoctor(user.getOrgId(), id);
        return true;
    }

}
