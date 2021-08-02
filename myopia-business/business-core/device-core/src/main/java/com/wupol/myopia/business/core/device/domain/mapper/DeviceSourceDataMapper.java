package com.wupol.myopia.business.core.device.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreenDataDTO;
import com.wupol.myopia.business.core.device.domain.model.DeviceSourceData;

import java.util.List;

/**
 * Mapper接口
 *
 * @Author jacob
 * @Date 2021-06-28
 */
public interface DeviceSourceDataMapper extends BaseMapper<DeviceSourceData> {

     /**
      * 多条件查找id
      * @param screeningOrgId
      * @param deviceSn
      * @param list 使用了 screeningOrgId  deviceSn patientId screeningTime 作为条件
      * @return 使用了 screeningOrgId  deviceSn patientId screeningTime 作为结果
      */
     List<DeviceScreenDataDTO> selectWithMutiConditions(Integer screeningOrgId, String deviceSn, List<DeviceScreenDataDTO> list);
}
