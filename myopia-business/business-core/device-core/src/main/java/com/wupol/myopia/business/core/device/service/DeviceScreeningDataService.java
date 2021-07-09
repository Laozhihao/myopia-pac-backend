package com.wupol.myopia.business.core.device.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.device.domain.dto.DeviceReportPrintResponseDTO;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreenDataDTO;
import com.wupol.myopia.business.core.device.domain.mapper.DeviceScreeningDataMapper;
import com.wupol.myopia.business.core.device.domain.model.Device;
import com.wupol.myopia.business.core.device.domain.model.DeviceScreeningData;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author jacob
 * @Date 2021-06-28
 */
@Service
public class DeviceScreeningDataService extends BaseService<DeviceScreeningDataMapper, DeviceScreeningData> {

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
     * 保存设备筛查数据
     * @param device
     * @param deviceScreenDataDTOList
     */
    public void saveDeviceScreeningDataList(Device device, List<DeviceScreenDataDTO> deviceScreenDataDTOList) {
        List<DeviceScreeningData> deviceScreeningDataList = getDeviceScreeningDataList(device, deviceScreenDataDTOList);
        //保存到src表中
        saveBatch(deviceScreeningDataList);
    }

    /**
     * 获取设备筛查数据list
     * @param device
     * @param deviceScreenDataDTOList
     * @return
     */
    private List<DeviceScreeningData> getDeviceScreeningDataList(Device device, List<DeviceScreenDataDTO> deviceScreenDataDTOList) {
        return  deviceScreenDataDTOList.stream().map(deviceScreenDataDTO->deviceScreenDataDTO.newDeviceScreeningDataInstance(device)).collect(Collectors.toList());
    }


}
