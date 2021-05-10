package com.wupol.myopia.business.hospital.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.hospital.service.HospitalInfoService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Map;

/**
 * 医院的App接口
 * @author Chikong
 * @date 2021-02-10
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/hospital/app")
public class HospitalInfoController {

    @Autowired
    private HospitalInfoService hospitalInfoService;


    @GetMapping()
    public Map<String, Object> getStudent() throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return hospitalInfoService.getHospitalInfo(user.getOrgId());
    }

}
