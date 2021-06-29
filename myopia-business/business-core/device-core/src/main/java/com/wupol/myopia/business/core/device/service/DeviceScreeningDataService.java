package com.wupol.myopia.business.core.device.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreeningDataAndOrgDTO;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreeningDataQueryDTO;
import com.wupol.myopia.business.core.device.domain.mapper.DeviceScreeningDataMapper;
import com.wupol.myopia.business.core.device.domain.model.DeviceScreeningData;
import org.springframework.stereotype.Service;

/**
 * @Author jacob
 * @Date 2021-06-28
 */
@Service
public class DeviceScreeningDataService extends BaseService<DeviceScreeningDataMapper, DeviceScreeningData> {

    public IPage<DeviceScreeningDataAndOrgDTO> selectPageByQuery(IPage<?> page, DeviceScreeningDataQueryDTO query) {
        return baseMapper.selectPageByQuery(page, query);
    }

}
