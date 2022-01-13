package com.wupol.myopia.business.aggregation.export.excel;

import com.alibaba.excel.write.merge.OnceAbsoluteMergeStrategy;
import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelFileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.DeviceScreeningDataExportDTO;
import com.wupol.myopia.business.core.screening.flow.service.DeviceScreeningDataExcelService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
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
    private DeviceScreeningDataExcelService deviceScreeningDataExcelService;
    @Autowired
    private ResourceFileService resourceFileService;

    @Override
    public List<DeviceScreeningDataExportDTO> getExcelData(ExportCondition exportCondition) {
        List<Integer> ids = exportCondition.getIds();
        return deviceScreeningDataExcelService.selectExcelData(ids);
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

    @Override
    public File generateExcelFile(String fileName, List data) throws IOException {
        OnceAbsoluteMergeStrategy mergeStrategy = new OnceAbsoluteMergeStrategy(0, 1, 20, 21);
        return ExcelUtil.exportListToExcel(fileName, data, mergeStrategy, DeviceScreeningDataExportDTO.class);
    }

    @Override
    public String syncExport(ExportCondition exportCondition) {
        File excelFile = null;
        try {
            // 1.获取文件名
            String fileName = getFileName(exportCondition);
            // 3.获取数据，生成List
            List<DeviceScreeningDataExportDTO> data = getExcelData(exportCondition);
            // 2.获取文件保存父目录路径
            excelFile = generateExcelFile(fileName, data);
            return resourceFileService.getResourcePath(s3Utils.uploadS3AndGetResourceFile(excelFile.getAbsolutePath(), fileName).getId());
        } catch (Exception e) {
            String requestData = JSON.toJSONString(exportCondition);
            log.error("【生成Excel异常】{}", requestData, e);
            // 发送失败通知
            throw new BusinessException("导出数据异常");
        } finally {
            // 5.删除临时文件
            if (Objects.nonNull(excelFile)) {
                deleteTempFile(excelFile.getPath());
            }
        }
    }

}
