package com.wupol.myopia.business.core.device.service;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreenDataDTO;
import com.wupol.myopia.business.core.device.domain.mapper.DeviceSourceDataMapper;
import com.wupol.myopia.business.core.device.domain.model.Device;
import com.wupol.myopia.business.core.device.domain.model.DeviceSourceData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author jacob
 * @Date 2021-06-28
 */
@Service
@Slf4j
public class DeviceSourceDataService extends BaseService<DeviceSourceDataMapper, DeviceSourceData> {

    @Autowired
    private DeviceSourceDataMapper deviceSourceDataMapper;
    /**
     * 过滤掉已经存在的数据
     * @param bindingScreeningOrgId
     * @param deviceScreenDataDTOList
     * @return
     */
    public List<DeviceScreenDataDTO> filterExistData(Integer bindingScreeningOrgId, String deviceSn, List<DeviceScreenDataDTO> deviceScreenDataDTOList) {
        List<DeviceScreenDataDTO> existDeviceScreeningDataDTO =  deviceSourceDataMapper.selectWithMutiConditions(bindingScreeningOrgId,deviceSn,deviceScreenDataDTOList);
        // 将存在的数据的唯一索引组成String Set
        Set<String> existSet = existDeviceScreeningDataDTO.stream().map(DeviceScreenDataDTO::getUnikeyString).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(existSet)) {
            return existDeviceScreeningDataDTO;
        }
        // 排除已经存在的数据
        return deviceScreenDataDTOList.stream().filter(deviceScreenDataDTO -> {
            deviceScreenDataDTO.setScreeningOrgId(bindingScreeningOrgId);
            deviceScreenDataDTO.setDeviceSn(deviceSn);
            String unikeyString = deviceScreenDataDTO.getUnikeyString();
            if (existSet.contains(unikeyString)) {
                log.warn("存在重复上传数据,唯一key: screeningOrgId-deviceSn-patientId-checkTime = {}", unikeyString);
                return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    /**
     * 保存设备上传的原始数据(目前只有vs666)
     * @param device
     * @param deviceScreenDataDTOList
     */
    public void saveDeviceSourceDataList(Device device, List<DeviceScreenDataDTO> deviceScreenDataDTOList) {
        List<DeviceSourceData> deviceSourceDataList = getDeviceSourceDataList(device, deviceScreenDataDTOList);
        //保存到src表中
        saveBatch(deviceSourceDataList);
    }


    /**
     * 获取DeviceSourceData 的 List
     * @param device
     * @param deviceScreenDataDTOList
     * @return
     */
    private List<DeviceSourceData> getDeviceSourceDataList(Device device, List<DeviceScreenDataDTO> deviceScreenDataDTOList) {
        return deviceScreenDataDTOList.stream().map(deviceScreenDataDTO -> DeviceSourceData.getNewInstance(device, JSON.toJSONString(deviceScreenDataDTO), deviceScreenDataDTO.getPatientId(), deviceScreenDataDTO.getCheckTime())).collect(Collectors.toList());
    }
}
