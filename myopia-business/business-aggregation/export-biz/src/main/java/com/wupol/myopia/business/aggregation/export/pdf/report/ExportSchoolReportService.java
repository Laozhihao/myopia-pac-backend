package com.wupol.myopia.business.aggregation.export.pdf.report;

import cn.hutool.core.util.StrUtil;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.pdf.BaseExportPdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.ExportPdfFileFactory;
import com.wupol.myopia.business.aggregation.export.pdf.ExportPdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.constant.ExportReportServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.Optional;

/**
 * 导出学校的筛查报告
 *
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Service(ExportReportServiceNameConstant.SCHOOL_SCREENING_REPORT_SERVICE)
public class ExportSchoolReportService extends BaseExportPdfFileService {

    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private ExportPdfFileFactory exportPdfFileFactory;
    @Autowired
    private ScreeningNoticeService screeningNoticeService;
    @Autowired
    private ScreeningPlanService screeningPlanService;

    /**
     * 生成文件
     *
     * @param exportCondition 导出条件
     * @param fileSavePath 文件保存路径
     * @param fileName 文件名
     * @return void
     **/
    @Override
    public void generatePdfFile(ExportCondition exportCondition, String fileSavePath, String fileName) {
        Optional<ExportPdfFileService> optional = getExportPdfFileService(exportCondition);
        optional.ifPresent(service -> service.generateSchoolReportPdfFile(fileSavePath,fileName,exportCondition));
    }

    /**
     * 获取文件名
     *
     * @param exportCondition 导出条件
     * @return java.lang.String
     **/
    @Override
    public String getFileName(ExportCondition exportCondition) {
        Optional<ExportPdfFileService> optional = getExportPdfFileService(exportCondition);
        return optional.map(service -> service.getFileName(exportCondition)).orElse(StrUtil.EMPTY);
    }

    @Override
    public void validateBeforeExport(ExportCondition exportCondition) {
        Assert.isTrue(Objects.nonNull(exportCondition.getNotificationId()) || Objects.nonNull(exportCondition.getPlanId()), "筛查通知ID和筛查计划ID都为空");
        int total = statConclusionService.count(new StatConclusion().setSrcScreeningNoticeId(exportCondition.getNotificationId()).setPlanId(exportCondition.getPlanId()).setSchoolId(exportCondition.getSchoolId()));
        if (total == 0) {
            throw new BusinessException("暂无筛查数据，无法导出筛查报告");
        }
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        return String.format(RedisConstant.FILE_EXPORT_PDF_SCHOOL_SCREENING,
                exportCondition.getApplyExportFileUserId(),
                exportCondition.getSchoolId(),
                exportCondition.getNotificationId(),
                exportCondition.getPlanId());
    }

    private Optional<ExportPdfFileService> getExportPdfFileService(ExportCondition exportCondition){
        Integer screeningType =null;
        if (Objects.nonNull(exportCondition.getNotificationId())){
            screeningType = screeningNoticeService.getById(exportCondition.getNotificationId()).getScreeningType();
        }
        if (Objects.isNull(screeningType) && Objects.nonNull(exportCondition.getPlanId())){
            screeningType = screeningPlanService.getById(exportCondition.getPlanId()).getScreeningType();
        }
        Assert.notNull(screeningType, "筛查通知ID和筛查计划ID都为空");
        ScreeningNotice screeningNotice = screeningNoticeService.getById(exportCondition.getNotificationId());
        return exportPdfFileFactory.getExportPdfFileService(screeningNotice.getScreeningType());
    }
}
