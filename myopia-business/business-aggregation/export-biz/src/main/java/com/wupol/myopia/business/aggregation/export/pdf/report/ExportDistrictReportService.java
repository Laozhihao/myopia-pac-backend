package com.wupol.myopia.business.aggregation.export.pdf.report;

import cn.hutool.core.util.StrUtil;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.domain.vo.PDFRequestDTO;
import com.wupol.myopia.business.aggregation.export.pdf.BaseExportPdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.ExportPdfFileFactory;
import com.wupol.myopia.business.aggregation.export.pdf.ExportPdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.constant.ExportReportServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 导出行政区域的筛查报告
 *
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Service(ExportReportServiceNameConstant.DISTRICT_SCREENING_REPORT_SERVICE)
public class ExportDistrictReportService extends BaseExportPdfFileService {

    @Autowired
    private ExportPdfFileFactory exportPdfFileFactory;
    @Autowired
    private ScreeningNoticeService screeningNoticeService;

    /**
     * 生成文件
     *
     * @param exportCondition 导出条件
     * @param fileSavePath    文件保存路径
     * @param fileName        文件名
     *
     * @return void
     **/
    @Override
    public void generatePdfFile(ExportCondition exportCondition, String fileSavePath, String fileName) {
        Optional<ExportPdfFileService> optional = getExportPdfFileService(exportCondition);
        optional.ifPresent(service -> service.generateDistrictReportPdfFile(fileSavePath, fileName, exportCondition));
    }

    /**
     * 获取文件名
     *
     * @param exportCondition 导出条件
     *
     * @return java.lang.String
     **/
    @Override
    public String getFileName(ExportCondition exportCondition) {
        Optional<ExportPdfFileService> optional = getExportPdfFileService(exportCondition);
        return optional.map(service -> service.getFileName(exportCondition)).orElse(StrUtil.EMPTY);
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        return String.format(RedisConstant.FILE_EXPORT_PDF_DISTRICT_SCREENING,
                exportCondition.getApplyExportFileUserId(),
                exportCondition.getNotificationId(),
                exportCondition.getDistrictId());
    }

    private Optional<ExportPdfFileService> getExportPdfFileService(ExportCondition exportCondition) {
        ScreeningNotice screeningNotice = screeningNoticeService.getById(exportCondition.getNotificationId());
        return exportPdfFileFactory.getExportPdfFileService(screeningNotice.getScreeningType());
    }

    @Override
    public PDFRequestDTO getAsyncRequestUrl(ExportCondition exportCondition) {
        Optional<ExportPdfFileService> optional = getExportPdfFileService(exportCondition);
        if (optional.isPresent()) {
            return optional.get().getDistrictReportPdfUrl(exportCondition);
        }
        return new PDFRequestDTO();
    }
}
