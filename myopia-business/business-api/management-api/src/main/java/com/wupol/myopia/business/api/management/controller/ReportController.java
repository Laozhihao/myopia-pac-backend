package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.pdf.constant.ExportReportServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Objects;

/**
 * @Author HaoHao
 * @Date 2021/3/16
 **/
@Validated
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/report")
@Log4j2
public class ReportController {

    @Autowired
    private ExportStrategy exportStrategy;

    /**
     * 导出区域的筛查报告 TODO: 权限校验、导出次数限制
     *
     * @param notificationId 筛查通知ID
     * @param districtId 行政区域ID
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @GetMapping("/district/export")
    public void exportDistrictReport(@NotNull(message = "筛查通知ID不能为空") Integer notificationId, @NotNull(message = "行政区域ID不能为空") Integer districtId) throws IOException {
        ExportCondition exportCondition = new ExportCondition()
                .setNotificationId(notificationId)
                .setDistrictId(districtId)
                .setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId());
        exportStrategy.doExport(exportCondition, ExportReportServiceNameConstant.DISTRICT_SCREENING_REPORT_SERVICE);
    }

    /**
     * 导出学校的筛查报告
     *
     * @param notificationId 筛查通知ID
     * @param planId 筛查计划ID
     * @param schoolId 学校ID
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @GetMapping("/school/export")
    public void exportSchoolReport(Integer notificationId, Integer planId, @NotNull(message = "学校ID不能为空") Integer schoolId) throws IOException {
        if (Objects.isNull(notificationId) && Objects.isNull(planId)) {
            throw new BusinessException("筛查通知ID或者筛查计划ID不能为空");
        }
        ExportCondition exportCondition = new ExportCondition()
                .setNotificationId(notificationId)
                .setPlanId(planId)
                .setSchoolId(schoolId)
                .setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId());
        exportStrategy.doExport(exportCondition, ExportReportServiceNameConstant.SCHOOL_SCREENING_REPORT_SERVICE);
    }

    /**
     * 导出筛查机构的筛查报告
     *
     * @param planId 筛查计划ID
     * @param screeningOrgId 行政区域ID
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @GetMapping("/screeningOrg/export")
    public void exportScreeningOrgReport(@NotNull(message = "筛查计划ID不能为空") Integer planId, @NotNull(message = "筛查机构ID不能为空") Integer screeningOrgId) throws IOException {
        ExportCondition exportCondition = new ExportCondition()
                .setPlanId(planId)
                .setScreeningOrgId(screeningOrgId)
                .setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId());
        exportStrategy.doExport(exportCondition, ExportReportServiceNameConstant.SCREENING_ORG_SCREENING_REPORT_SERVICE);
    }

    /**
     * 导出学校档案卡
     *
     * @param planId 筛查计划ID
     * @param schoolId 学校ID
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @GetMapping("/school/archives")
    public void exportSchoolArchives(@NotNull(message = "筛查计划ID不能为空") Integer planId, @NotNull(message = "学校ID不能为空") Integer schoolId) throws IOException {
        ExportCondition exportCondition = new ExportCondition()
                .setPlanId(planId)
                .setSchoolId(schoolId)
                .setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId());
        exportStrategy.doExport(exportCondition, ExportReportServiceNameConstant.SCHOOL_ARCHIVES_SERVICE);
    }

    /**
     * 导出筛查机构档案卡
     *
     * @param planId 筛查计划ID
     * @param screeningOrgId 筛查机构ID
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @GetMapping("/screeningOrg/archives")
    public void exportScreeningOrgArchives(@NotNull(message = "筛查计划ID不能为空") Integer planId, @NotNull(message = "筛查机构ID不能为空") Integer screeningOrgId,
                                           @NotNull(message = "筛查机构ID不能为空") Integer schoolId, Integer classId,
                                           Integer gradeId, @RequestParam(value="planStudentIds", required = false) String planStudentIds) throws IOException {
        ExportCondition exportCondition = new ExportCondition()
                .setPlanId(planId).setScreeningOrgId(screeningOrgId)
                .setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId())
                .setSchoolId(schoolId)
                .setClassId(classId)
                .setGradeId(gradeId)
                .setPlanStudentIds(planStudentIds);
        exportStrategy.doExport(exportCondition, ExportReportServiceNameConstant.SCREENING_ORG_ARCHIVES_SERVICE);
    }

}
