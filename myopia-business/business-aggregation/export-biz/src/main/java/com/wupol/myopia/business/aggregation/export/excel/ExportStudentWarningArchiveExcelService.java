package com.wupol.myopia.business.aggregation.export.excel;

import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelNoticeKeyContentConstant;
import com.wupol.myopia.business.aggregation.export.excel.domain.StudentWarningArchive;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.hospital.service.MedicalReportService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 导出学生预警跟踪档案Excel
 *
 * @Author HaoHao
 * @Date 2021/10/22
 **/
@Service("studentWarningArchiveExcelService")
public class ExportStudentWarningArchiveExcelService extends BaseExportExcelFileService {

    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private MedicalReportService medicalReportService;

    @Override
    public List getExcelData(ExportCondition exportCondition) {
        // 1.获取当前学校下的筛查数据
        List<StatConclusionExportDTO> statConclusionExportList = statConclusionService.getExportVoByScreeningPlanIdAndSchoolId(exportCondition.getPlanId(), exportCondition.getSchoolId());
        // 2.遍历构建Excel实体
        /*statConclusionExportList.stream().map(statConclusionExport -> {

            BeanUtils.copyProperties(statConclusionExport, );
        })*/
        return null;
    }

    @Override
    public Class getHeadClass() {
        return StudentWarningArchive.class;
    }

    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {
        return getFileName(exportCondition);
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        ScreeningPlan plan = screeningPlanService.getById(exportCondition.getPlanId());
        return String.format(ExcelNoticeKeyContentConstant.STUDENT_WARNING_ARCHIVE_EXCEL_NOTICE_KEY_CONTENT,
                plan.getTitle(),
                DateFormatUtil.format(plan.getStartTime(), DateFormatUtil.FORMAT_ONLY_DATE),
                DateFormatUtil.format(plan.getEndTime(), DateFormatUtil.FORMAT_ONLY_DATE));
    }

    @Override
    public String getRedisKey(ExportCondition exportCondition) {
        return null;
    }

    @Override
    public void validateBeforeExport(ExportCondition exportCondition) {
        Assert.notNull(exportCondition.getScreeningOrgId(), "筛查机构ID不能为空");
        Assert.notNull(exportCondition.getPlanId(), "筛查计划ID不能为空");
        Assert.notNull(exportCondition.getSchoolId(), "学校ID不能为空");
    }
}
