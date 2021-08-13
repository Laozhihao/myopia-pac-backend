package com.wupol.myopia.business.core.device.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.business.core.device.domain.dto.DeviceReportPrintResponseDTO;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreenDataDTO;
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

    /**
     * 多条件查找id
     * @param screeningOrgId
     * @param deviceSn
     * @param list 使用了 screeningOrgId  deviceSn patientId screeningTime 作为条件
     * @return 使用了 screeningOrgId  deviceSn patientId screeningTime 作为结果
     */
    List<DeviceScreenDataDTO> selectWithMutiConditions(Integer screeningOrgId, String deviceSn, List<DeviceScreenDataDTO> list);

}
