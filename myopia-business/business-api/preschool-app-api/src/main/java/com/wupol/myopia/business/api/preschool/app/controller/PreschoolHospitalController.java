package com.wupol.myopia.business.api.preschool.app.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author wulizhou
 * @Date 2022/1/14 12:17
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/preschool/app/hospital")
public class PreschoolHospitalController {

    @Autowired
    private HospitalService hospitalService;

    /**
     * 通过医院名称获取医院列表
     * @param name
     * @return
     */
    @GetMapping("/byName")
    public List<Hospital> getByName(String name) {
        return hospitalService.getHospitalByName(name, null).stream()
                .map(h -> new Hospital().setId(h.getId())
                .setName(h.getName())).collect(Collectors.toList());
    }

}
