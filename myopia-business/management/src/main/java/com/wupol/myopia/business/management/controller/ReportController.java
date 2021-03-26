package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.management.export.ExportStrategy;
import com.wupol.myopia.business.management.export.constant.ExportReportServiceNameConstant;
import com.wupol.myopia.business.management.export.domain.ExportCondition;
import com.wupol.myopia.business.management.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.management.service.StatConclusionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;
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
    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    /**
     * 导出区域的筛查报告 TODO: 权限校验、导出次数限制
     *
     * @param notificationId 筛查通知ID
     * @param districtId 行政区域ID
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @GetMapping("/district/export")
    public ApiResult exportDistrictReport(@NotNull(message = "筛查通知ID不能为空") Integer notificationId, @NotNull(message = "行政区域ID不能为空") Integer districtId) {
        ExportCondition exportCondition = new ExportCondition().setNotificationId(notificationId).setDistrictId(districtId).setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId());
        exportStrategy.doExport(exportCondition, ExportReportServiceNameConstant.DISTRICT_SCREENING_REPORT_SERVICE);
        return ApiResult.success();
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
    public ApiResult exportSchoolReport(Integer notificationId, Integer planId, @NotNull(message = "学校ID不能为空") Integer schoolId) {
        if (Objects.isNull(notificationId) && Objects.isNull(planId)) {
            return ApiResult.failure("筛查通知ID或者筛查计划ID不能为空");
        }
        ExportCondition exportCondition = new ExportCondition().setNotificationId(notificationId).setPlanId(planId).setSchoolId(schoolId).setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId());
        exportStrategy.doExport(exportCondition, ExportReportServiceNameConstant.SCHOOL_SCREENING_REPORT_SERVICE);
        return ApiResult.success();
    }

    /**
     * 导出筛查机构的筛查报告
     *
     * @param planId 筛查计划ID
     * @param screeningOrgId 行政区域ID
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @GetMapping("/screeningOrg/export")
    public ApiResult exportScreeningOrgReport(@NotNull(message = "筛查计划ID不能为空") Integer planId, @NotNull(message = "筛查机构ID不能为空") Integer screeningOrgId) {
        List<Integer> schoolIdList = statConclusionService.getSchoolIdByPlanId(planId);
        if (CollectionUtils.isEmpty(schoolIdList)) {
            return ApiResult.failure("暂无筛查数据，无法导出筛查报告");
        }
        ExportCondition exportCondition = new ExportCondition().setPlanId(planId).setScreeningOrgId(screeningOrgId).setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId());
        exportStrategy.doExport(exportCondition, ExportReportServiceNameConstant.SCREENING_ORG_SCREENING_REPORT_SERVICE);
        return ApiResult.success();
    }

    /**
     * 导出学校档案卡
     *
     * @param planId 筛查计划ID
     * @param schoolId 学校ID
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @GetMapping("/school/archives")
    public ApiResult exportSchoolArchives(@NotNull(message = "筛查计划ID不能为空") Integer planId, @NotNull(message = "学校ID不能为空") Integer schoolId) {
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = screeningPlanSchoolStudentService.getByScreeningPlanId(planId);
        if (CollectionUtils.isEmpty(screeningPlanSchoolStudentList)) {
            return ApiResult.failure("次计划下该学校暂无筛查学生数据，无法导出档案卡");
        }
        ExportCondition exportCondition = new ExportCondition().setPlanId(planId).setSchoolId(schoolId).setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId());
        exportStrategy.doExport(exportCondition, ExportReportServiceNameConstant.SCHOOL_ARCHIVES_SERVICE);
        return ApiResult.success();
    }

    /**
     * 导出筛查机构档案卡
     *
     * @param planId 筛查计划ID
     * @param screeningOrgId 筛查机构ID
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @GetMapping("/screeningOrg/archives")
    public ApiResult exportScreeningOrgArchives(@NotNull(message = "筛查计划ID不能为空") Integer planId, @NotNull(message = "筛查机构ID不能为空") Integer screeningOrgId) {
        ExportCondition exportCondition = new ExportCondition().setPlanId(planId).setScreeningOrgId(screeningOrgId).setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId());
        exportStrategy.doExport(exportCondition, ExportReportServiceNameConstant.SCREENING_ORG_ARCHIVES_SERVICE);
        return ApiResult.success();
    }

}
