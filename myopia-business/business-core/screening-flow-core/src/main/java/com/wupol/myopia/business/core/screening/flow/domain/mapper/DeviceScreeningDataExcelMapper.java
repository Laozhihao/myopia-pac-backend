package com.wupol.myopia.business.core.screening.flow.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.screening.flow.domain.dto.DeviceScreeningDataExportDTO;

import java.util.List;


public interface DeviceScreeningDataExcelMapper extends BaseMapper<DeviceScreeningDataExportDTO> {

    List<DeviceScreeningDataExportDTO> selectExcelData(List<Integer> ids);
}
