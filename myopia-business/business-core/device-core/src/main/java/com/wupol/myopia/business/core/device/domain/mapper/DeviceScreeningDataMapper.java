package com.wupol.myopia.business.core.device.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.device.domain.dto.DeviceReportPrintResponseDTO;
import com.wupol.myopia.business.core.device.domain.model.DeviceScreeningData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Mapper接口
 *
 * @Author jacob
 * @Date 2021-06-28
 */
public interface DeviceScreeningDataMapper extends BaseMapper<DeviceScreeningData> {

    List<DeviceReportPrintResponseDTO> getByIds(@Param("ids") List<Integer> ids);

}
