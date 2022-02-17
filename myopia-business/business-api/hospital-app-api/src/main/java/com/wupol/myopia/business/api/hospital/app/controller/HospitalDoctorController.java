package com.wupol.myopia.business.api.hospital.app.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.core.hospital.domain.dto.DoctorDTO;
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

    @GetMapping
    public DoctorDTO getDoctorInfo() {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return hospitalDoctorService.getDetailsByUserId(user.getId());
    }

    @PutMapping
    public Boolean saveDoctor(@RequestBody @Valid DoctorDTO doctor) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        DoctorDTO oldDoctor = hospitalDoctorService.getDetailsByUserId(user.getId());
        doctor.setId(oldDoctor.getId());
        doctor.setPhone(null);
        doctor.setUserId(user.getId());
        hospitalDoctorService.updateDoctor(doctor);
        return true;
    }

    @GetMapping("/list")
    public List<DoctorDTO> getDoctorList(DoctorQuery query) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        query.setHospitalId(user.getOrgId());
        return hospitalDoctorService.getDoctorDTOList(query);
    }

}
