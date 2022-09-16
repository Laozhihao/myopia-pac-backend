package com.wupol.myopia.business.api.hospital.app.service;

import com.wupol.myopia.business.common.utils.domain.dto.DeviceGrantedDTO;
import com.wupol.myopia.business.common.utils.domain.model.ScreeningConfig;
import com.wupol.myopia.business.core.device.constant.OrgTypeEnum;
import com.wupol.myopia.business.core.device.domain.model.Device;
import com.wupol.myopia.business.core.device.service.DeviceService;
import com.wupol.myopia.business.core.hospital.domain.model.MedicalRecord;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.business.core.hospital.service.MedicalRecordService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private DeviceService deviceService;

    /**
     * 获取医院信息
     * @param hospitalId 医院id
     * @return
     */
    public Map<String, Object> getHospitalInfo(Integer hospitalId) {
        Map<String, Object> map = new HashMap<>(3);
        // 累计就诊的人数
        map.put("totalMedicalPersonCount", medicalRecordService.count(new MedicalRecord().setHospitalId(hospitalId)));
        map.put("name", hospitalService.getById(hospitalId).getName());
        return map;
    }

    /**
     * 获取医院配置
     *
     * @return ApiResult<ScreeningConfig>
     */
    public ScreeningConfig getHospitalScreeningConfig(Integer hospitalId) {
        ScreeningConfig screeningConfig = new ScreeningConfig();
        List<Device> deviceList = deviceService.getByOrgIdAndOrgType(hospitalId, OrgTypeEnum.HOSPITAL.getCode());
        if (CollectionUtils.isEmpty(deviceList)) {
            return screeningConfig;
        }
        List<DeviceGrantedDTO> grantedDTOS = deviceList.stream().map(s -> {
            DeviceGrantedDTO deviceGrantedDTO = new DeviceGrantedDTO();
            deviceGrantedDTO.setDeviceSn(s.getDeviceSn());
            deviceGrantedDTO.setType(s.getType());
            deviceGrantedDTO.setBluetoothMac(s.getBluetoothMac());
            deviceGrantedDTO.setStatus(s.getStatus());
            return deviceGrantedDTO;
        }).collect(Collectors.toList());
        return screeningConfig.setGrantedDeviceList(grantedDTOS);
    }


}