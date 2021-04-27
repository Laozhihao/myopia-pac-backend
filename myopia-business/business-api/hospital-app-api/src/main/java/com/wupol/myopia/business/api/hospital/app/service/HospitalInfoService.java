package com.wupol.myopia.business.api.hospital.app.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalAdmin;
import com.wupol.myopia.business.core.hospital.domain.model.MedicalRecord;
import com.wupol.myopia.business.core.hospital.domain.query.HospitalQuery;
import com.wupol.myopia.business.core.hospital.service.HospitalAdminService;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.business.core.hospital.service.MedicalRecordService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Map<String, Object> map = new HashMap<>(3);
        // 累计就诊的人数
        map.put("totalMedicalPersonCount", medicalRecordService.count(new MedicalRecord().setHospitalId(hospitalId)));
        map.put("name", hospitalService.getById(hospitalId).getName());
        return map;
    }


}