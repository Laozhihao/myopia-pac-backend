package com.wupol.myopia.business.hospital.service;

import com.wupol.myopia.business.hospital.domain.model.MedicalRecord;
import com.wupol.myopia.business.management.service.HospitalService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 医院-信息
 * @author Chikong
 * @date 2021-02-10
 */
@Service
@Log4j2
public class HospitalInfoService {

    @Autowired
    private MedicalRecordService medicalRecordService;
    @Autowired
    private HospitalService hospitalService;

    /**
     * 获取医院信息
     * @param hospitalId 医院id
     * @return
     */
    public Map<String, Object> getHospitalInfo(Integer hospitalId) throws IOException {
        Map<String, Object> map = new HashMap<>();
        // 累计就诊的人数
        map.put("totalMedicalPersonCount", medicalRecordService.count(new MedicalRecord().setHospitalId(hospitalId)));
        map.put("name", hospitalService.getById(hospitalId).getName());

        return map;
    }

}