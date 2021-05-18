package com.wupol.myopia.business.aggregation.export;

import com.wupol.myopia.business.aggregation.export.interfaces.ExportFileService;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Log4j2
@Service
public class ExportStrategy {

    @Autowired
    Map<String, ExportFileService> exportFileServiceMap = new ConcurrentHashMap<>(8);

    public void doExport(ExportCondition exportCondition, String serviceName) throws IOException {
        ExportFileService exportFileService = getExportFileService(serviceName);
        exportFileService.validateBeforeExport(exportCondition);
        exportFileService.export(exportCondition);
    }

    private ExportFileService getExportFileService(String serviceName) {
        ExportFileService exportFileService = exportFileServiceMap.get(serviceName);
        Assert.notNull(exportFileService, "没有找到对应导出文件service实例：" + serviceName);
        return exportFileService;
    }
}
