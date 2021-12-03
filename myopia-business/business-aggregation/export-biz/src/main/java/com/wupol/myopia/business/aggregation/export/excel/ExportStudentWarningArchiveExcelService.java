package com.wupol.myopia.business.aggregation.export.excel;

import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelNoticeKeyContentConstant;
import com.wupol.myopia.business.aggregation.export.excel.domain.StudentWarningArchive;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.common.utils.util.VisionUtil;
import com.wupol.myopia.business.core.hospital.domain.model.MedicalReport;
import com.wupol.myopia.business.core.hospital.service.MedicalReportService;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 导出学生预警跟踪档案Excel
 *
 * @Author HaoHao
 * @Date 2021/10/22
 **/
@Service("studentWarningArchiveExcelService")
public class ExportStudentWarningArchiveExcelService extends BaseExportExcelFileService {

    private static final String DESK_AND_CHAIR_TYPE_SUGGEST = "%scm。课桌：%s，建议桌面高：%d。课椅：%s，建议座面高：%d。";
    private static final String SEAT_DISTANCE_SUGGEST = "与黑板相距5-6米";
    private static final String EMPTY_DATA = "--";

    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private MedicalReportService medicalReportService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;

    @Override
    public List getExcelData(ExportCondition exportCondition) {
        // 1.获取当前学校下的筛查数据
        List<StatConclusionExportDTO> statConclusionExportList = statConclusionService.getExportVoByScreeningPlanIdAndSchoolId(exportCondition.getPlanId(), exportCondition.getSchoolId());
        // 2.获取年级信息(同个学校的年级名称不存在相同的)
        List<Integer> gradeIdList = statConclusionExportList.stream().map(StatConclusionExportDTO::getGradeId).distinct().collect(Collectors.toList());
        Map<Integer, String> gradeNameMap = schoolGradeService.getClassNameMapByIds(gradeIdList);
        // 3.获取年级班级信息
        List<Integer> classIdList = statConclusionExportList.stream().map(StatConclusionExportDTO::getClassId).distinct().collect(Collectors.toList());
        Map<Integer, String> classNameMap = schoolClassService.getClassNameMapByIds(classIdList);
        // 4.遍历构建Excel数据
        return statConclusionExportList.stream().map(statConclusionExport -> {
            StudentWarningArchive studentWarningArchive = new StudentWarningArchive();
            BeanUtils.copyProperties(statConclusionExport, studentWarningArchive);
            studentWarningArchive.setSno(statConclusionExport.getStudentNo())
                    .setStudentName(statConclusionExport.getStudentName())
                    .setGenderDesc(GenderEnum.getName(statConclusionExport.getGender()))
                    .setGradeAndClassName(gradeNameMap.get(statConclusionExport.getGradeId()) + "-" + classNameMap.get(statConclusionExport.getClassId()))
                    .setVisionStatus(VisionUtil.getVisionSummary(statConclusionExport.getGlassesType(), statConclusionExport.getMyopiaLevel(), statConclusionExport.getHyperopiaLevel(), statConclusionExport.getAstigmatismLevel()))
                    .setVisionWarning(WarningLevel.getDesc(statConclusionExport.getWarningLevel()))
                    // 系统暂时没有身高数据，写死null
                    .setDeskAndChairTypeSuggest(getDeskAndChairTypeSuggest(null, statConclusionExport.getSchoolAge()))
                    .setSeatDistanceSuggest(StatUtil.isMyopia(statConclusionExport.getMyopiaLevel()) ? SEAT_DISTANCE_SUGGEST : StringUtils.EMPTY);
            // 设置就诊信息
            setVisitInfo(studentWarningArchive, statConclusionExport);
            return studentWarningArchive;
        }).collect(Collectors.toList());
    }

    /**
     * 获取课桌椅建议
     *
     * @param height 身高
     * @param schoolAge 学龄
     * @return string
     **/
    private String getDeskAndChairTypeSuggest(Float height, Integer schoolAge) {
        if (Objects.isNull(height) || Objects.isNull(schoolAge)) {
            return EMPTY_DATA;
        }
        List<Integer> deskAndChairType = SchoolAge.KINDERGARTEN.code.equals(schoolAge) ? DeskChairTypeEnum.getKindergartenTypeByHeight(height) : DeskChairTypeEnum.getPrimarySecondaryTypeByHeight(height);
        String deskAndChairTypeDesc = deskAndChairType.stream().map(x -> x + "号").collect(Collectors.joining("或"));
        return String.format(DESK_AND_CHAIR_TYPE_SUGGEST, String .format("%.1f", height), deskAndChairTypeDesc, (int) (height * 0.43), deskAndChairTypeDesc, (int) (height * 0.24));
    }

    /**
     * 设置就诊信息
     *
     * @param studentWarningArchive   预计跟踪档案
     * @param statConclusionExportDTO 筛查统计记录
     */
    private void setVisitInfo(StudentWarningArchive studentWarningArchive, StatConclusionExportDTO statConclusionExportDTO) {
        Integer reportId = statConclusionExportDTO.getReportId();
        if (Objects.isNull(reportId)) {
            studentWarningArchive.setIsVisited("未去");
            return;
        }
        MedicalReport report = medicalReportService.getById(reportId);
        if (Objects.isNull(report)) {
            studentWarningArchive.setIsVisited("未去");
            return;
        }
        studentWarningArchive.setIsVisited("已去")
                .setGlassesSuggest(WearingGlassesSuggestEnum.getDescByCode(report.getGlassesSituation()))
                .setVisitResult(report.getMedicalContent());
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
    public String getLockKey(ExportCondition exportCondition) {
        return String.format(RedisConstant.FILE_EXPORT_EXCEL_STUDENT_WARNING_ARCHIVE,
                exportCondition.getApplyExportFileUserId(),
                exportCondition.getPlanId(),
                exportCondition.getSchoolId());
    }

    @Override
    public void validateBeforeExport(ExportCondition exportCondition) {
        Assert.notNull(exportCondition.getScreeningOrgId(), "筛查机构ID不能为空");
        Assert.notNull(exportCondition.getPlanId(), "筛查计划ID不能为空");
        Assert.notNull(exportCondition.getSchoolId(), "学校ID不能为空");
    }
}
