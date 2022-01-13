package com.wupol.myopia.business.core.screening.flow.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.DeviceScreeningDataExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.DeviceScreeningDataExcelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 钓猫的小鱼
 * @Date: 2022/01/13/19:54
 * @Description:
 */
@Service
public class ScreeningDataExcelService  extends BaseService<DeviceScreeningDataExcelMapper, DeviceScreeningDataExportDTO> {

    public List<DeviceScreeningDataExportDTO> findByDataList(List<Integer> ids) {
        return baseMapper.selectExcelData(ids);
    }


}
