package com.wupol.myopia.business.core.device.service;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.framework.core.util.StringUtils;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.device.domain.dto.DeviceReportPrintResponseDTO;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreenDataDTO;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreeningDataAndOrgDTO;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreeningDataQueryDTO;
import com.wupol.myopia.business.core.device.domain.mapper.DeviceScreeningDataMapper;
import com.wupol.myopia.business.core.device.domain.model.Device;
import com.wupol.myopia.business.core.device.domain.model.DeviceScreeningData;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author jacob
 * @Date 2021-06-28
 */
@Service
public class DeviceScreeningDataService extends BaseService<DeviceScreeningDataMapper, DeviceScreeningData> {

    public IPage<DeviceScreeningDataAndOrgDTO> selectPageByQuery(IPage<?> page, DeviceScreeningDataQueryDTO query) {
        return baseMapper.selectPageByQuery(page, query);
    }

    /**
     * 获取打印需要的信息
     *
     * @param ids 需要打印的Ids
     * @return List<DeviceReportPrintResponseDTO>
     */
    public List<DeviceReportPrintResponseDTO> getPrintReportInfo(List<Integer> ids) {
        return baseMapper.getByIds(ids);
    }


    /**
     * 更新或者插入数据
     * @param device
     * @param updateOrSaveDataMap
     */
    public void updateOrAddDeviceScreeningDataList(Device device, Map<Boolean, List<DeviceScreenDataDTO>> updateOrSaveDataMap) {
        //更新
        updateBatchByUniBusinessKey(getDeviceScreeningDataList(device, updateOrSaveDataMap.get(true)));
        //保存到src表中
        saveBatch(getDeviceScreeningDataList(device, updateOrSaveDataMap.get(false)));
    }

    /**
     * 更新数据: deviceSn,screeningTime,screeningOrgId,patientId 这四个条件必须存在
     * @param deviceScreeningDataList
     */
    private void updateBatchByUniBusinessKey(List<DeviceScreeningData> deviceScreeningDataList) {
            for (DeviceScreeningData deviceScreeningData: deviceScreeningDataList) {

                String deviceSn = deviceScreeningData.getDeviceSn();
                Integer screeningOrgId = deviceScreeningData.getScreeningOrgId();
                String patientId = deviceScreeningData.getPatientId();
                Date screeningTime = deviceScreeningData.getScreeningTime();

                if (ObjectUtil.hasEmpty(deviceSn,screeningTime,screeningOrgId,patientId)) {
                    continue;
                }
                LambdaQueryWrapper<DeviceScreeningData> deviceSourceDataLambdaQueryWrapper = new LambdaQueryWrapper<>();
                deviceSourceDataLambdaQueryWrapper
                        .eq(DeviceScreeningData::getDeviceSn,deviceScreeningData.getDeviceSn())
                        .eq(DeviceScreeningData::getScreeningOrgId,deviceScreeningData.getScreeningOrgId())
                        .eq(DeviceScreeningData::getPatientId, deviceScreeningData.getPatientId())
                        .eq(DeviceScreeningData::getScreeningTime,deviceScreeningData.getScreeningTime());
                update(deviceScreeningData,deviceSourceDataLambdaQueryWrapper);
            }
    }


    /**
     * 获取设备筛查数据list
     * @param device
     * @param deviceScreenDataDTOList
     * @return
     */
    private List<DeviceScreeningData> getDeviceScreeningDataList(Device device, List<DeviceScreenDataDTO> deviceScreenDataDTOList) {
        if (CollectionUtils.isEmpty(deviceScreenDataDTOList)) {
            return Collections.emptyList();
        }
        return  deviceScreenDataDTOList.stream().map(deviceScreenDataDTO->deviceScreenDataDTO.newDeviceScreeningDataInstance(device)).collect(Collectors.toList());
    }

    /**
     * 根据以下条件查找数据
     * @param screeningOrgId
     * @param deviceSn
     * @param deviceScreenDataDTOList
     * @return
     */
    public List<DeviceScreenDataDTO> listBatchWithMutiConditions(Integer screeningOrgId, String deviceSn, List<DeviceScreenDataDTO> deviceScreenDataDTOList){
        if (screeningOrgId == null || StringUtils.isBlank(deviceSn) || CollectionUtils.isEmpty(deviceScreenDataDTOList)) {
            logger.warn("更新deviceScreenData数据异常,存在为空的数据, screeningOrgId = {} ,deviceSn = {}, deviceScreenDataDTOList = {} ",screeningOrgId, deviceSn, JSON.toJSONString(deviceScreenDataDTOList));
        }
        return baseMapper.selectWithMutiConditions(screeningOrgId, deviceSn, deviceScreenDataDTOList);
    }
}
