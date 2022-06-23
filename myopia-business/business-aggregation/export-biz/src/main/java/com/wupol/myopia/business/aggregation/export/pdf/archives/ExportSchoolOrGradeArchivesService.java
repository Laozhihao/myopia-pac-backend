package com.wupol.myopia.business.aggregation.export.pdf.archives;

import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.business.aggregation.export.pdf.BaseExportPdfFileService;
import com.wupol.myopia.business.aggregation.export.pdf.constant.ArchiveExportTypeEnum;
import com.wupol.myopia.business.aggregation.export.pdf.constant.ExportReportServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.constant.PDFFileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.BizMsgConstant;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * 【学校/年级维度】导出档案卡
 *
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Service(ExportReportServiceNameConstant.EXPORT_SCHOOL_OR_GRADE_ARCHIVES_SERVICE)
public class ExportSchoolOrGradeArchivesService extends BaseExportPdfFileService {

    @Autowired
    private ArchivePdfGenerator archivePdfGenerator;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private ScreeningPlanService screeningPlanService;

    /**
     * 生成文件
     *
     * @param exportCondition 导出条件
     * @param fileSavePath    文件保存路径
     * @param fileName        文件名
     **/
    @Override
    public void generatePdfFile(ExportCondition exportCondition, String fileSavePath, String fileName) {
        archivePdfGenerator.generateSchoolOrGradeArchivesPdfFile(fileSavePath, exportCondition);
    }

    /**
     * 前置处理
     *
     * @param exportCondition 导出条件
     * @return void
     **/
    @Override
    public void preProcess(ExportCondition exportCondition) {
        ScreeningPlan screeningPlan = screeningPlanService.getById(exportCondition.getPlanId());
        exportCondition.setScreeningOrgId(screeningPlan.getScreeningOrgId()).setScreeningType(screeningPlan.getScreeningType());
    }

    /**
     * 获取文件名
     *
     * @param exportCondition 导出条件
     * @return java.lang.String
     **/
    @Override
    public String getFileName(ExportCondition exportCondition) {
        School school = schoolService.getById(exportCondition.getSchoolId());
        String fileName = school.getName() + Optional.ofNullable(exportCondition.getGradeId()).map(x -> schoolGradeService.getById(x).getName()).orElse(StringUtils.EMPTY);
        return String.format(PDFFileNameConstant.ARCHIVES_PDF_FILE_NAME, fileName, ScreeningTypeEnum.isVisionScreeningType(exportCondition.getScreeningType()) ? PDFFileNameConstant.VISION_ARCHIVE : PDFFileNameConstant.COMMON_DISEASE_ARCHIVE);
    }

    /**
     * 导出前参数校验
     *
     * @param exportCondition 导出条件
     * @return void
     **/
    @Override
    public void validateBeforeExport(ExportCondition exportCondition) {
        Assert.notNull(exportCondition.getType(), BizMsgConstant.EXPORT_TYPE_IS_NULL);
        Assert.notNull(exportCondition.getScreeningType(), BizMsgConstant.SCREENING_TYPE_IS_NULL);
        Assert.notNull(exportCondition.getPlanId(), BizMsgConstant.PLAN_ID_IS_NULL);
        Assert.notNull(exportCondition.getSchoolId(), BizMsgConstant.SCHOOL_ID_IS_NULL);
        if (ArchiveExportTypeEnum.GRADE.getType().equals(exportCondition.getType())) {
            Assert.notNull(exportCondition.getGradeId(), BizMsgConstant.GRADE_ID_IS_NULL);
        }

        VisionScreeningResult visionScreeningResult = new VisionScreeningResult()
                .setScreeningOrgId(exportCondition.getScreeningOrgId())
                .setPlanId(exportCondition.getPlanId())
                .setSchoolId(exportCondition.getSchoolId())
                .setIsDoubleScreen(Boolean.FALSE);
        int total = visionScreeningResultService.count(visionScreeningResult);
        Assert.isTrue(total > 0, "该计划下暂无筛查学生数据，无法导出档案卡");
    }

    /**
     * 获取锁
     *
     * @param exportCondition 导出条件
     * @return java.lang.String
     **/
    @Override
    public String getLockKey(ExportCondition exportCondition) {
        return String.format(RedisConstant.FILE_EXPORT_PDF_ARCHIVES_ORG,
                exportCondition.getApplyExportFileUserId(),
                exportCondition.getPlanId(),
                exportCondition.getSchoolId(),
                exportCondition.getClassId(),
                exportCondition.getGradeId(),
                exportCondition.getPlanStudentIds());
    }

}
