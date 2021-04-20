package com.wupol.myopia.business.api.hospital.app.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.hospital.app.domain.vo.DoctorVo;
import com.wupol.myopia.business.api.hospital.app.service.DoctorService;
import com.wupol.myopia.business.core.hospital.domain.model.Doctor;
import com.wupol.myopia.business.core.hospital.domain.query.DoctorQuery;
import com.wupol.myopia.business.core.hospital.service.HospitalDoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    @Autowired
    private DoctorService doctorService;

    @GetMapping("/list")
    public List<DoctorVo> getDoctorList(DoctorQuery query) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        query.setHospitalId(user.getOrgId());
        return doctorService.getDoctorVoList(query);
    }

    @GetMapping("/{id}")
    public DoctorVo getDoctor(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return doctorService.getDoctorVo(user.getOrgId(), id);
    }

    @PostMapping()
    public Boolean saveDoctor(@RequestBody @Valid Doctor doctor) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        hospitalDoctorService.saveDoctor(user, doctor);
        return true;
    }

    @DeleteMapping("/{id}")
    public Boolean deleteDoctor(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        hospitalDoctorService.deleteDoctor(user.getOrgId(), id);
        return true;
    }

}