package com.wupol.myopia.business.core.device.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.device.domain.dto.DeviceReportPrintResponseDTO;
import com.wupol.myopia.business.core.device.domain.mapper.DeviceScreeningDataMapper;
import com.wupol.myopia.business.core.device.domain.model.DeviceScreeningData;
import org.springframework.stereotype.Service;

import java.util.List;

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

}
