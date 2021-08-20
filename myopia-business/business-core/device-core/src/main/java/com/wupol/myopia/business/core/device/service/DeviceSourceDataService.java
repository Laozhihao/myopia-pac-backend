package com.wupol.myopia.business.core.device.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.framework.core.util.StringUtils;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreenDataDTO;
import com.wupol.myopia.business.core.device.domain.mapper.DeviceSourceDataMapper;
import com.wupol.myopia.business.core.device.domain.model.Device;
import com.wupol.myopia.business.core.device.domain.model.DeviceSourceData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
     * 根据条件以下条件查找数据
     * @param screeningOrgId
     * @param deviceSn
     * @param deviceScreenDataDTOList
     * @return
     */
    public List<DeviceScreenDataDTO> listBatchWithMutiConditions(Integer screeningOrgId, String deviceSn, List<DeviceScreenDataDTO> deviceScreenDataDTOList){
        if (screeningOrgId == null || StringUtils.isBlank(deviceSn) || CollectionUtils.isEmpty(deviceScreenDataDTOList)) {
            log.warn("更新deviceScreenData数据异常,存在为空的数据, screeningOrgId = {} ,deviceSn = {}, deviceScreenDataDTOList = {} ",screeningOrgId, deviceSn, JSON.toJSONString(deviceScreenDataDTOList));
        }
        return deviceSourceDataMapper.selectWithMutiConditions(screeningOrgId, deviceSn, deviceScreenDataDTOList);
    }


    /**
     * 保存设备上传的原始数据(目前只有vs666)
     *
     * @param device
     * @param deviceScreenDataDTOList
     */
    public void saveDeviceSourceDataList(Device device, List<DeviceScreenDataDTO> deviceScreenDataDTOList) {
        //保存到src表中
        saveBatch(getDeviceSourceDataList(device, deviceScreenDataDTOList));
    }

    /**
     * 更新或者保存数据
     *
     * @param device
     * @param updateOrSaveDataMap
     */
    public void updateOrAddDeviceSourceDataList(Device device, Map<Boolean, List<DeviceScreenDataDTO>> updateOrSaveDataMap ) {
        //更新
        updateDeviceSourceDataList(device, updateOrSaveDataMap.get(true));
        //保存
        saveDeviceSourceDataList(device, updateOrSaveDataMap.get(false));
    }


    /**
     * 更新数据
     *
     * @param device
     * @param deviceScreenDataDTOList
     */
    private void updateDeviceSourceDataList(Device device, List<DeviceScreenDataDTO> deviceScreenDataDTOList) {
        updateDeviceSourceDataList(getDeviceSourceDataList(device, deviceScreenDataDTOList));
    }

    /**
     * geng'ju
     * @param deviceSourceDataList
     */
    private void updateDeviceSourceDataList(List<DeviceSourceData> deviceSourceDataList) {
        if (CollectionUtils.isEmpty(deviceSourceDataList)) {
            return;
        }
        for (DeviceSourceData deviceSourceData : deviceSourceDataList) {
            LambdaQueryWrapper<DeviceSourceData> deviceSourceDataLambdaQueryWrapper = new LambdaQueryWrapper<>();
            deviceSourceDataLambdaQueryWrapper
                    .eq(DeviceSourceData::getDeviceSn, deviceSourceData.getDeviceSn())
                    .eq(DeviceSourceData::getScreeningOrgId, deviceSourceData.getScreeningOrgId())
                    .eq(DeviceSourceData::getPatientId, deviceSourceData.getPatientId())
                    .eq(DeviceSourceData::getScreeningTime, deviceSourceData.getScreeningTime());
            update(deviceSourceData, deviceSourceDataLambdaQueryWrapper);
        }
    }


    /**
     * 获取DeviceSourceData 的 List
     *
     * @param device
     * @param deviceScreenDataDTOList
     * @return
     */
    private List<DeviceSourceData> getDeviceSourceDataList(Device device, List<DeviceScreenDataDTO> deviceScreenDataDTOList) {
        if (CollectionUtils.isEmpty(deviceScreenDataDTOList)) {
            return Collections.emptyList();
        }
        return deviceScreenDataDTOList.stream().map(deviceScreenDataDTO -> DeviceSourceData.getNewInstance(device, JSON.toJSONString(deviceScreenDataDTO), deviceScreenDataDTO.getPatientId(), deviceScreenDataDTO.getCheckTime())).collect(Collectors.toList());
    }
}
