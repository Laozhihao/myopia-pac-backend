package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.model.Doctor;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.service.HospitalDoctorService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @Author wulizhou
 * @Date 2021/11/30 14:34
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/doctor")
public class DoctorController extends BaseController<HospitalDoctorService, Doctor> {

    /**
     * 添加医生
     * @param doctor
     * @return
     */
    @PostMapping
    public UsernameAndPasswordDTO saveDoctor(@RequestBody @Valid Doctor doctor) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        doctor.setCreateUserId(user.getId())
            .setDepartmentId(-1);
        return baseService.saveDoctor(doctor);
    }

    /**
     * 更新医院
     *
     * @param hospital 医院实体
     * @return 医院实体
     */
    @PutMapping
    public HospitalResponseDTO updateDoctor(@RequestBody @Valid Doctor hospital) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        hospital.setCreateUserId(user.getId());
        return hospitalBizService.updateHospital(hospital);
    }

}
