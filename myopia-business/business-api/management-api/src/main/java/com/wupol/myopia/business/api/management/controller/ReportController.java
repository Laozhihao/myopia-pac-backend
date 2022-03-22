package com.wupol.myopia.business.api.management.controller;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.PdfResponseDTO;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.pdf.constant.ExportReportServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.core.common.service.Html2PdfService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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
    private Html2PdfService html2PdfService;

    @Value("${report.html.url-host}")
    public String htmlUrlHost;
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
    public void exportScreeningOrgReport(@NotNull(message = "筛查计划ID不能为空") Integer planId,
                                         @NotNull(message = "筛查机构ID不能为空") Integer screeningOrgId,
                                         @NotNull(message = "学校ID不能为空") Integer schoolId) throws IOException {
        ExportCondition exportCondition = new ExportCondition()
                .setSchoolId(schoolId)
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
    public void exportSchoolArchives(@NotNull(message = "筛查计划ID不能为空") Integer planId,
                                     @NotNull(message = "学校ID不能为空") Integer schoolId,
                                     Integer classId,
                                     Integer gradeId,
                                     @NotNull(message = "筛查机构ID不能为空") Integer screeningOrgId,
                                     Integer districtId,
                                     @RequestParam(value="planStudentIds", required = false) String planStudentIds)
            throws IOException {
        ExportCondition exportCondition = new ExportCondition()
                .setPlanId(planId)
                .setSchoolId(schoolId)
                .setClassId(classId)
                .setGradeId(gradeId)
                .setPlanStudentIds(planStudentIds)
                .setScreeningOrgId(screeningOrgId)
                .setDistrictId(districtId)
                .setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId());
        exportStrategy.doExport(exportCondition, ExportReportServiceNameConstant.SCHOOL_ARCHIVES_SERVICE);
    }

    /**
     * 导出行政区域档案卡
     *
     * @param notificationId 筛查通知ID
     * @param districtId 行政区域ID
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @GetMapping("/district/archives")
    public void exportDistrictArchives(@NotNull(message = "筛查通知ID不能为空") Integer notificationId,
                                     @NotNull(message = "行政区域ID不能为空") Integer districtId) throws IOException {
        ExportCondition exportCondition = new ExportCondition()
                .setNotificationId(notificationId)
                .setDistrictId(districtId)
                .setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId());
        exportStrategy.doExport(exportCondition, ExportReportServiceNameConstant.EXPORT_DISTRICT_ARCHIVES_SERVICE);
    }

    /**
     * 导出筛查机构档案卡
     *
     * @param planId 筛查计划ID
     * @param screeningOrgId 筛查机构ID
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @GetMapping("/screeningOrg/archives")
    public void exportScreeningOrgArchives(@NotNull(message = "筛查计划ID不能为空") Integer planId,
                                           @NotNull(message = "筛查机构ID不能为空") Integer screeningOrgId,
                                           Integer schoolId,
                                           Integer classId,
                                           Integer gradeId,
                                           @RequestParam(value="planStudentIds", required = false) String planStudentIds) throws IOException {
        ExportCondition exportCondition = new ExportCondition()
                .setPlanId(planId)
                .setScreeningOrgId(screeningOrgId)
                .setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId())
                .setSchoolId(schoolId)
                .setClassId(classId)
                .setGradeId(gradeId)
                .setPlanStudentIds(planStudentIds);

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
    public ApiResult<String> syncExportSchoolStudentArchives(
                                                             String planStudentIds,
                                                             Integer classId,
                                                             Integer gradeId,
                                                             Integer schoolId,
                                                             @NotNull(message = "筛查机构ID不能为空") Integer screeningOrgId,
                                                             @NotNull(message = "筛查计划ID不能为空") Integer planId) {
        if (StringUtils.isNotBlank(planStudentIds)) {
            List<Integer> planStudentIdList = Arrays.stream(planStudentIds.split(",")).map(Integer::valueOf).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(visionScreeningResultService.getByPlanStudentIds(planStudentIdList))) {
                throw new BusinessException("所选学生无筛查数据");
            }
        }

        ExportCondition exportCondition = new ExportCondition()
                .setPlanId(planId)
                .setScreeningOrgId(screeningOrgId)
                .setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId())
                .setSchoolId(schoolId)
                .setClassId(classId)
                .setGradeId(gradeId)
                .setPlanStudentIds(planStudentIds);

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
                                       Integer schoolId) throws IOException {

        ExportCondition exportCondition = new ExportCondition()
                .setPlanId(planId)
                .setScreeningOrgId(screeningOrgId)
                .setSchoolId(schoolId)
                .setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId());

        exportStrategy.doExport(exportCondition, ExportReportServiceNameConstant.SCREENING_PLAN);
    }

    /**
     *
     * @param screeningPlanId 筛查计划ID
     * @param schoolId 学校ID
     * @param gradeId 年级ID
     * @param classId 班级ID
     * @param planStudentIds 学生集会
     * @param type
     * @return
     * @throws IOException
     */
    @GetMapping("/screeningOrg/qrcode")
    public ApiResult<String> getScreeningStudentQrCode(@NotNull(message = "筛查计划ID不能为空") Integer screeningPlanId,
                                          @NotNull(message = "学校ID不能为空") Integer schoolId,
                                          Integer gradeId,
                                          Integer classId,
                                          String planStudentIds,
                                          @NotNull(message = "TypeID不能为空") Integer type
                                          ) throws IOException {

        ExportCondition exportCondition = new ExportCondition()
                .setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId())
                .setPlanId(screeningPlanId)
                .setSchoolId(schoolId)
                .setGradeId(gradeId)
                .setClassId(classId)
                .setPlanStudentIds(planStudentIds)
                .setType(type)
                ;
        if (classId!=null|| StringUtil.isNotEmpty(planStudentIds)){
            return ApiResult.success(exportStrategy.syncExport(exportCondition, ExportReportServiceNameConstant.EXPORT_QRCODE_SCREENING_SERVICE));
        }
        exportStrategy.doExport(exportCondition, ExportReportServiceNameConstant.EXPORT_QRCODE_SCREENING_SERVICE);
        return ApiResult.success();
    }

    /**
     * 0-6岁PDF
     *
     * @param type 类型 0-转诊单 1-检查记录表 2-回执单
     * @param id   id
     * @return pdf文件
     */
    @GetMapping("preSchool/pdf")
    public ApiResult<String> preSchoolPdf(@NotNull(message = "type不能为空") Integer type,
                                          @NotNull(message = "id不能为空") Integer id) {
        Integer clientId = Integer.valueOf(CurrentUserUtil.getCurrentUser().getClientId());
        String userToken = CurrentUserUtil.getUserToken();

        boolean isHospital = SystemCode.HOSPITAL_CLIENT.getCode().equals(clientId) || SystemCode.PRESCHOOL_CLIENT.getCode().equals(clientId);
        String url = StringUtils.EMPTY;

        switch (type) {
            case 0:
                url = String.format(ReportConst.REFERRAL_PDF_URL, htmlUrlHost, id, isHospital, userToken);
                break;
            case 1:
                url = String.format(ReportConst.EXAMINE_PDF_URL, htmlUrlHost, id, isHospital, userToken);
                break;
            case 2:
                url = String.format(ReportConst.RECEIPT_PDF_URL, htmlUrlHost, id, isHospital, userToken);
                break;
            default:
        }
        if (StringUtils.isBlank(url)) {
            return ApiResult.failure("URL为空");
        }
        return ApiResult.success(html2PdfService.syncGeneratorPDF(url, "报告.pdf", UUID.randomUUID().toString()).getUrl());
    }

}
