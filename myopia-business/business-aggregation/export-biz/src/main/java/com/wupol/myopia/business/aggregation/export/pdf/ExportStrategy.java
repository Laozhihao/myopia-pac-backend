package com.wupol.myopia.business.aggregation.export.pdf;

import com.wupol.framework.core.util.StringUtils;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.aggregation.export.pdf.interfaces.ExportFileService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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

    @Async
    public void doExport(ExportCondition exportCondition, String serviceName) {
        ExportFileService exportFileService = getExportFileService(serviceName);
        try {
            exportFileService.export(exportCondition);
        } catch (Exception e) {
            log.error("导出文件-生成文件名或文件保存路径时异常", e);
            exportFileService.sendFailNotice(exportCondition.getApplyExportFileUserId(), StringUtils.EMPTY);
        }
    }

    private ExportFileService getExportFileService(String serviceName) {
        ExportFileService exportFileService = exportFileServiceMap.get(serviceName);
        Assert.notNull(exportFileService, "没有找到对应导出文件service实例：" + serviceName);
        return exportFileService;
    }
}
