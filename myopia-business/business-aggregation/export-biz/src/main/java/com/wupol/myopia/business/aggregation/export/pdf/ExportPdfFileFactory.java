package com.wupol.myopia.business.aggregation.export.pdf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 导出pdf文件工厂
 *
 * @author hang.yuan 2022/6/23 16:21
 */
@Component
public class ExportPdfFileFactory {

    @Autowired
    private List<ExportPdfFileService> exportPdfFileServiceList;

    public Optional<ExportPdfFileService> getExportPdfFileService(Integer screeningType){
        return exportPdfFileServiceList.stream()
                .filter(service -> Objects.equals(service.getScreeningType(),screeningType))
                .findFirst();
    }

}
