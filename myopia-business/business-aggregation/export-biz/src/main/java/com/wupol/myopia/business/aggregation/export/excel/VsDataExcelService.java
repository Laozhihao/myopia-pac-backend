package com.wupol.myopia.business.aggregation.export.excel;

import cn.hutool.core.util.ZipUtil;
import com.alibaba.excel.write.merge.OnceAbsoluteMergeStrategy;
import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelFileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.DeviceScreeningDataExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionExportDTO;
import com.wupol.myopia.business.core.screening.flow.service.DeviceScreeningDataExcelService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
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
    private DeviceScreeningDataExcelService deviceScreeningDataExcelService;
    @Autowired
    private ResourceFileService resourceFileService;

    @Override
    public List getExcelData(ExportCondition exportCondition) {
        List<Integer> ids = exportCondition.getIds();
        List<DeviceScreeningDataExportDTO> deviceScreeningData  = deviceScreeningDataExcelService.selectExcelData(ids);

        return deviceScreeningData;
    }

    @Override
    public Class getHeadClass() {
        return StatConclusionExportDTO.class;
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

    @Override
    public File generateExcelFile(String fileName, List data) throws IOException {

        List<DeviceScreeningDataExportDTO> deviceScreeningData = data;

        String path = UUID.randomUUID() + "/"+fileName;
        OnceAbsoluteMergeStrategy mergeStrategy = new OnceAbsoluteMergeStrategy(0, 1, 20, 21);
        File excelFile =   ExcelUtil.exportListToExcel(path, deviceScreeningData, mergeStrategy, DeviceScreeningDataExportDTO.class);

return excelFile;
//        return ZipUtil.zip(StringUtils.substringBeforeLast(StringUtils.substringBeforeLast(StringUtils.substringBeforeLast(excelFile.getAbsolutePath(), "/"), "/"), "/"));
    }

    @Override
    public String syncExport(ExportCondition exportCondition) {
        String parentPath = null;
        File excelFile = null;
        try {
            // 1.获取文件名
            String fileName = getFileName(exportCondition);
            // 3.获取数据，生成List
            List data = getExcelData(exportCondition);
            // 2.获取文件保存父目录路径
            excelFile = generateExcelFile(fileName, data);
            return resourceFileService.getResourcePath(s3Utils.uploadS3AndGetResourceFile(excelFile.getAbsolutePath(), fileName).getId());
        } catch (Exception e) {
            String requestData = JSON.toJSONString(exportCondition);
            log.error("【生成报告异常】{}", requestData, e);
            // 发送失败通知
            throw new BusinessException("导出数据异常");
        } finally {
            // 5.删除临时文件
            deleteTempFile(parentPath);
        }
    }

    /**
     * 获取文件同步导出文件名称
     * @param exportCondition
     * @return
     */
    private String getFileNameTitle(ExportCondition exportCondition){

        return null;
    }
}
