package com.wupol.myopia.business.core.device.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.business.core.device.domain.dto.DeviceReportPrintResponseDTO;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreeningDataAndOrgDTO;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreeningDataQueryDTO;
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

    IPage<DeviceScreeningDataAndOrgDTO> selectPageByQuery(@Param("page") IPage<?> page, @Param("param") DeviceScreeningDataQueryDTO query);

    List<DeviceReportPrintResponseDTO> getByIds(@Param("ids") List<Integer> ids);

}
