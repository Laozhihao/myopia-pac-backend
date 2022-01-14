package com.wupol.myopia.business.aggregation.export.excel;

import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelFileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreeningDataExportDTO;
import com.wupol.myopia.business.core.device.service.DeviceScreeningDataService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.UUID;

/**
 * 导出vs666数据
 *
 * @author tastyb
 */
@Log4j2
@Service("vsDataExcelService")
public class VsDataExcelService extends BaseExportExcelFileService {

    @Autowired
    private DeviceScreeningDataService deviceScreeningDataService;


    @Override
    public List<DeviceScreeningDataExportDTO> getExcelData(ExportCondition exportCondition) {
        return deviceScreeningDataService.findByDataList(exportCondition.getIds());
    }

    @Override
    public Class getHeadClass() {
        return DeviceScreeningDataExportDTO.class;
    }

    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {
        return null;
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        return String.format(ExcelFileNameConstant.VS_EQUIPMENT_FILE_NAME, UUID.randomUUID().toString());
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        return null;
    }


}
