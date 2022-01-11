package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.pdf.constant.ExportReportServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.api.management.service.SysUtilService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private VisionScreeningResultService visionScreeningResultService;

    @Autowired
    private SysUtilService sysUtilService;
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
                .setPlanId(planId)
                .setScreeningOrgId(screeningOrgId)
                .setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId())
                .setSchoolId(schoolId)
                .setClassId(classId)
                .setGradeId(gradeId)
                .setPlanStudentIds(planStudentIds);


        if (CurrentUserUtil.getCurrentUser().getUserType()==1){
            String key =  String.format(RedisConstant.FILE_EXPORT_ARCHIVES_COUNT,
                    "exportScreeningOrgArchives",planId,screeningOrgId,CurrentUserUtil.getCurrentUser().getId(),schoolId,gradeId,classId,planStudentIds);
            sysUtilService.isExport(key);
        }


        exportStrategy.doExport(exportCondition, ExportReportServiceNameConstant.SCREENING_ORG_ARCHIVES_SERVICE);
    }

    /**
     * 批量打印学生档案卡
     *
     * @param planStudentIds 学生Id
     * @param schoolId       学校Id
     * @param planId         计划Id
     * @return 文件URL
     */
    @GetMapping("/school/student/archives")
    public ApiResult<String> syncExportSchoolStudentArchives(@RequestParam(value = "planStudentIds") String planStudentIds,
                                                             @RequestParam(value = "schoolId") Integer schoolId,
                                                             @RequestParam(value = "planId") Integer planId) {
        List<Integer> planStudentIdList = Arrays.stream(planStudentIds.split(",")).map(Integer::valueOf).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(visionScreeningResultService.getByPlanStudentIds(planStudentIdList))) {
            throw new BusinessException("所选学生无筛查数据");
        }

        if (CurrentUserUtil.getCurrentUser().getUserType()==1){
            String key =  String.format(RedisConstant.FILE_EXPORT_EXCEL_ARCHIVES_COUNT,
                    "syncExportSchoolStudentArchives",CurrentUserUtil.getCurrentUser().getId(),planId, schoolId, planStudentIds);
            sysUtilService.isExport(key);
        }

        ExportCondition exportCondition = new ExportCondition()
                .setPlanStudentIds(planStudentIds)
                .setSchoolId(schoolId)
                .setPlanId(planId);
        return ApiResult.success(exportStrategy.syncExport(exportCondition, ExportReportServiceNameConstant.STUDENT_ARCHIVES_SERVICE));
    }


    /**
    * @Description: 导出学校筛查报告PDF
    * @Param: [筛检计划ID, 筛查机构ID, 学校ID]
    * @return: void
    * @Author: 钓猫的小鱼
    * @Date: 2021/12/30
    */
    @GetMapping("/screeningOrg/export/school")
    public void getScreeningPlanSchool(@NotNull(message = "筛查计划ID不能为空") Integer planId,
                                       @NotNull(message = "筛查机构ID不能为空") Integer screeningOrgId,
                                       @NotNull(message = "学校ID不能为空") Integer schoolId) throws IOException {

        ExportCondition exportCondition = new ExportCondition()
                .setPlanId(planId)
                .setScreeningOrgId(screeningOrgId)
                .setSchoolId(schoolId)
                .setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId());

        if (CurrentUserUtil.getCurrentUser().getUserType()==1){
            String key =  String.format(RedisConstant.FILE_EXPORT_PDF_COUNT,
                    "getScreeningPlanSchool",exportCondition.getApplyExportFileUserId(), exportCondition.getPlanId(), exportCondition.getSchoolId());
            sysUtilService.isExport(key);
        }
        exportStrategy.doExport(exportCondition, ExportReportServiceNameConstant.SCREENING_PLAN);
    }
}
