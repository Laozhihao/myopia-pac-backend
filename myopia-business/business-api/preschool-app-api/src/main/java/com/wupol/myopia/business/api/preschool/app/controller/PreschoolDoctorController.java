package com.wupol.myopia.business.api.preschool.app.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.core.hospital.domain.dto.DoctorDTO;
import com.wupol.myopia.business.core.hospital.service.HospitalDoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 0-6岁APP接口
 *
 * @Author wulizhou
 * @Date 2021/12/2 17:35
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/preschool/app/doctor")
public class PreschoolDoctorController {

    @Autowired
    private HospitalDoctorService hospitalDoctorService;

    @GetMapping
    public DoctorDTO getDoctorInfo() {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return hospitalDoctorService.getDetailsByUserId(user.getId());
    }

}
