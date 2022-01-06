package com.wupol.myopia.business.core.screening.flow.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.DeviceScreeningDataExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.DeviceScreeningDataExcelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceScreeningDataExcelService  extends BaseService<DeviceScreeningDataExcelMapper, DeviceScreeningDataExportDTO> {


    public List<DeviceScreeningDataExportDTO> selectExcelData(List<Integer> ids) {

        return baseMapper.selectExcelData(ids);
    }
}
