package com.wupol.myopia.business.aggregation.export.excel;

import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelFileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.util.CheckModeEnum;
import com.wupol.myopia.business.common.utils.util.CheckTypeEnum;
import com.wupol.myopia.business.common.utils.util.PatientAgeUtil;
import com.wupol.myopia.business.common.utils.util.VS666Util;
import com.wupol.myopia.business.core.device.domain.dto.DeviceReportPrintResponseDTO;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreeningDataExportDTO;
import com.wupol.myopia.business.core.device.service.DeviceScreeningDataService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
        List<DeviceReportPrintResponseDTO> reportList = deviceScreeningDataService.getPrintReportInfo(exportCondition.getIds());
        List<DeviceScreeningDataExportDTO> exportDTOS = new ArrayList<>();
        reportList.forEach(report -> {
            DeviceScreeningDataExportDTO exportDTO = new DeviceScreeningDataExportDTO();
            exportDTO.setId(report.getPatientId());
            exportDTO.setPatientName(report.getPatientName());
            exportDTO.setPatientGender(GenderEnum.getName(report.getPatientGender()));
            exportDTO.setPatientAgeGroup(PatientAgeUtil.getAgeRange(report.getPatientAgeGroup()));
            exportDTO.setPatientOrg(report.getPatientOrg());
            exportDTO.setPatientDept(report.getPatientDept());
            exportDTO.setPatientPno(report.getPatientPno());
            exportDTO.setCheckMode(CheckModeEnum.getName(report.getCheckMode()));
            exportDTO.setCheckType(CheckTypeEnum.getName(report.getCheckType()));
            exportDTO.setRightSph(formatDate(report.getRightSph()));
            exportDTO.setRightCyl(formatDate(report.getRightCyl()));
            exportDTO.setRightAxsi(report.getRightAxsi());
            exportDTO.setRightPa(formatDate(report.getRightPa()));
            exportDTO.setLeftSph(formatDate(report.getLeftSph()));
            exportDTO.setLeftCyl(formatDate(report.getLeftCyl()));
            exportDTO.setLeftAxsi(report.getLeftAxsi());
            exportDTO.setLeftPa(formatDate(report.getLeftPa()));
            exportDTO.setRightPr(report.getRightPr());
            exportDTO.setLeftPr(report.getLeftPr());
            exportDTO.setRightAxsiV(report.getRightAxsiV());
            exportDTO.setLeftAxsiV(report.getLeftAxsiV());
            exportDTO.setRightAxsiH(report.getRightAxsiH());
            exportDTO.setLeftAxsiH(report.getLeftAxsiH());
            exportDTO.setRedReflectRight(report.getRedReflectRight());
            exportDTO.setRedReflectLeft(report.getRedReflectLeft());
            exportDTO.setPd(report.getPd());
            exportDTO.setCheckResult(report.getCheckResult());
            exportDTOS.add(exportDTO);
        });
        return exportDTOS;
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
        return ExcelFileNameConstant.VS_EQUIPMENT_FILE_NAME;
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        return null;
    }

    /**
     * 格式化数据
     *
     * @param val 值
     * @return 值
     */
    private String formatDate(Double val) {
        if (val >= 0d) {
            return "+" + VS666Util.getDisplayValue(val);
        }
        return String.valueOf(VS666Util.getDisplayValue(val));
    }

}
