package com.wupol.myopia.business.api.management.controller;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.pdf.archives.SyncExportStudentScreeningArchivesService;
import com.wupol.myopia.business.aggregation.export.pdf.constant.ExportReportServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.PrimarySchoolVisionReportDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.SchoolStudentResponseDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.kindergarten.KindergartenRefractiveSituationDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.kindergarten.KindergartenVisionReportDTO;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseaseReportVO;
import com.wupol.myopia.business.api.management.domain.vo.report.SchoolCommonDiseaseReportVO;
import com.wupol.myopia.business.api.management.service.CommonDiseaseReportService;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.ScreeningAreaReportDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten.KindergartenReportDTO;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.PrimaryReportDTO;
import com.wupol.myopia.business.api.management.service.ScreeningAreaReportService;
import com.wupol.myopia.business.api.management.service.ScreeningKindergartenReportService;
import com.wupol.myopia.business.api.management.service.ScreeningPrimaryReportService;
import com.wupol.myopia.business.api.management.service.report.refactor.KindergartenVisionReportService;
import com.wupol.myopia.business.api.management.service.report.refactor.PrimarySchoolVisionReportService;
import com.wupol.myopia.business.core.common.service.Html2PdfService;
import com.wupol.myopia.business.core.hospital.domain.dto.ReceiptDTO;
import com.wupol.myopia.business.core.hospital.service.PreschoolCheckRecordService;
import com.wupol.myopia.business.core.hospital.service.ReceiptListService;
import com.wupol.myopia.business.core.hospital.service.ReferralRecordService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
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

    @Value("${report.html.url-host}")
    public String htmlUrlHost;


    @Autowired
    private ExportStrategy exportStrategy;

    @Autowired
    private Html2PdfService html2PdfService;

    @Autowired
    private ReferralRecordService referralRecordService;

    @Autowired
    private PreschoolCheckRecordService preschoolCheckRecordService;

    @Autowired
    private ReceiptListService receiptListService;

    @Autowired
    private SyncExportStudentScreeningArchivesService syncExportStudentScreeningArchivesService;

    @Autowired
    private ScreeningAreaReportService screeningAreaReportService;

    @Autowired
    private ScreeningPrimaryReportService screeningPrimaryReportService;

    @Autowired
    private ScreeningKindergartenReportService screeningKindergartenReportService;

    @Autowired
    private CommonDiseaseReportService commonDiseaseReportService;

    @Autowired
    private PrimarySchoolVisionReportService primarySchoolVisionReportService;

    @Autowired
    private KindergartenVisionReportService kindergartenVisionReportService;

    /**
     * 导出区域的筛查报告 TODO: 权限校验、导出次数限制
     *
     * @param notificationId 筛查通知ID
     * @param districtId     行政区域ID
     *
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @GetMapping("/district/export")
    public void exportDistrictReport(@NotNull(message = "筛查通知ID不能为空") Integer notificationId,
                                     @NotNull(message = "行政区域ID不能为空") Integer districtId,
                                     @NotNull(message = "是否幼儿园不能为空") Boolean isKindergarten,
                                     @NotNull(message = "导出类型不能为空") Integer type) throws IOException {
        ExportCondition exportCondition = new ExportCondition()
                .setNotificationId(notificationId)
                .setDistrictId(districtId)
                .setIsKindergarten(isKindergarten)
                .setExportType(type)
                .setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId());

        exportStrategy.doAsyncExport(exportCondition, ExportReportServiceNameConstant.DISTRICT_SCREENING_REPORT_SERVICE);
    }

    /**
     * 导出学校的筛查报告
     *
     * @param planId   筛查计划ID
     * @param schoolId 学校ID
     *
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @GetMapping("/school/export")
    public void exportSchoolReport(@NotNull(message = "筛查计划ID不能为空") Integer planId,
                                   @NotNull(message = "学校ID不能为空") Integer schoolId,
                                   @NotNull(message = "导出类型不能为空") Integer type) throws IOException {
        ExportCondition exportCondition = new ExportCondition()
                .setPlanId(planId)
                .setSchoolId(schoolId)
                .setExportType(type)
                .setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId());
        exportStrategy.doAsyncExport(exportCondition, ExportReportServiceNameConstant.SCHOOL_SCREENING_REPORT_SERVICE);
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
     * @param screeningPlanId 筛查计划ID
     * @param schoolId        学校ID
     * @param gradeId         年级ID
     * @param classId         班级ID
     * @param planStudentIds  学生集会
     * @param type
     *
     * @return
     *
     * @throws IOException
     */
    @GetMapping("/screeningOrg/qrcode")
    public ApiResult<String> getScreeningStudentQrCode(@NotNull(message = "筛查计划ID不能为空") Integer screeningPlanId,
                                                       @NotNull(message = "学校ID不能为空") Integer schoolId,
                                                       Integer gradeId,
                                                       Integer classId,
                                                       String planStudentIds,
                                                       @NotNull(message = "TypeID不能为空") Integer type) throws IOException {

        ExportCondition exportCondition = new ExportCondition()
                .setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId())
                .setPlanId(screeningPlanId)
                .setSchoolId(schoolId)
                .setGradeId(gradeId)
                .setClassId(classId)
                .setPlanStudentIds(planStudentIds)
                .setType(type)
                .setIsSchoolClient(Boolean.FALSE);
        if (classId != null || StringUtil.isNotEmpty(planStudentIds)) {
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
     *
     * @return pdf文件
     */
    @GetMapping("pdf")
    public ApiResult<String> preSchoolPdf(@NotBlank(message = "type不能为空") String type,
                                          @NotNull(message = "id不能为空") Integer id) {
        Integer clientId = Integer.valueOf(CurrentUserUtil.getCurrentUser().getClientId());
        String userToken = CurrentUserUtil.getUserToken();

        boolean isHospital = SystemCode.HOSPITAL_CLIENT.getCode().equals(clientId) || SystemCode.PRESCHOOL_CLIENT.getCode().equals(clientId);
        String url = StringUtils.EMPTY;

        if (StringUtils.equals(ReportConst.TYPE_REFERRAL, type)) {
            if (Objects.isNull(referralRecordService.getDetailById(id))) {
                throw new BusinessException("找不到该转诊单");
            }
            url = String.format(ReportConst.REFERRAL_PDF_URL, htmlUrlHost, id, isHospital, userToken);
        }
        if (StringUtils.equals(ReportConst.TYPE_EXAMINE, type)) {
            if (Objects.isNull(preschoolCheckRecordService.getDetail(id))) {
                throw new BusinessException("找不到该检查记录表");
            }
            url = String.format(ReportConst.EXAMINE_PDF_URL, htmlUrlHost, id, isHospital, userToken);
        }
        if (StringUtils.equals(ReportConst.TYPE_RECEIPT, type)) {
            ReceiptDTO receipt = receiptListService.getDetail(new ReceiptDTO().setPreschoolCheckRecordId(id));
            if (Objects.isNull(receipt) || Objects.isNull(receiptListService.getDetailById(receipt.getId()))) {
                throw new BusinessException("找不到该回执单");
            }
            url = String.format(ReportConst.RECEIPT_PDF_URL, htmlUrlHost, id, isHospital, userToken);
        }
        if (StringUtils.isBlank(url)) {
            return ApiResult.failure("根据Type找不到对应URL");
        }
        return ApiResult.success(html2PdfService.syncGeneratorPDF(url, "报告.pdf").getUrl());
    }

    /**
     * 学生档案卡路径
     *
     * @param resultId   结果ID
     * @param templateId 模板ID
     *
     * @return
     */
    @GetMapping("/student/archivesUrl")
    public ApiResult<String> syncExportArchivesPdfUrl(@NotNull(message = "结果ID") Integer resultId,
                                                      @NotNull(message = "模板ID") Integer templateId) {

        return ApiResult.success(syncExportStudentScreeningArchivesService.generateArchivesPdfUrl(resultId, templateId));
    }

    /**
     * 视力筛查-区域
     *
     * @return ScreeningAreaReportDTO
     */
    @GetMapping("/screening/areaReport")
    public ScreeningAreaReportDTO areaReport(Integer noticeId, Integer districtId) {
        return screeningAreaReportService.generateReport(noticeId, districtId);
    }

    /**
     * 视力筛查-小学及以上
     *
     * @return PrimaryReportDTO
     */
    @GetMapping("/screening/primaryReport")
    public PrimaryReportDTO primaryReport(Integer planId, Integer schoolId) {
        return screeningPrimaryReportService.generateReport(planId, schoolId);
    }

    /**
     * 视力筛查-幼儿园
     *
     * @return KindergartenReportDTO
     */
    @GetMapping("/screening/kindergartenReport")
    public KindergartenReportDTO kindergartenReport(Integer planId, Integer schoolId) {
        return screeningKindergartenReportService.generateReport(planId, schoolId);

    }

    /**
     * 按区域常见病报告
     * @param districtId 区域ID
     * @param noticeId 通知ID
     *
     */
    @GetMapping("/districtCommonDiseaseReport")
    public ApiResult<DistrictCommonDiseaseReportVO> districtCommonDiseaseReport(@RequestParam Integer districtId,
                                                                               @RequestParam Integer noticeId){
        return ApiResult.success(commonDiseaseReportService.districtCommonDiseaseReport(districtId,noticeId));
    }

    /**
     * 按学校常见病报告
     * @param schoolId 学校ID
     * @param planId 计划ID（当筛查通知ID为空时，此值必填）
     */
    @GetMapping("/schoolCommonDiseaseReport")
    public ApiResult<SchoolCommonDiseaseReportVO> schoolCommonDiseaseReport(@RequestParam Integer schoolId,
                                                                            @RequestParam Integer planId){
        return ApiResult.success(commonDiseaseReportService.schoolCommonDiseaseReport(schoolId,planId));
    }

    /**
     * 重构报告-中小学
     *
     * @param planId   计划Id
     * @param schoolId 学校Id
     *
     * @return ApiResult<PrimarySchoolVisionReportDTO>
     */
    @GetMapping("/refactor/school/primary")
    public ApiResult<PrimarySchoolVisionReportDTO> refactorPrimarySchoolVisionReport(Integer planId, Integer schoolId) {
        return ApiResult.success(primarySchoolVisionReportService.primarySchoolVisionReport(planId, schoolId));
    }

    /**
     * 重构报告-判断学生类型
     *
     * @param planId   计划Id
     * @param schoolId 学校Id
     *
     * @return ApiResult<SchoolStudentResponseDTO>
     */
    @GetMapping("/refactor/school/studentType")
    public ApiResult<SchoolStudentResponseDTO> refactorSchoolStudentType(Integer planId, Integer schoolId) {
        return ApiResult.success(primarySchoolVisionReportService.schoolStudentType(planId, schoolId));
    }

    /**
     * 重构报告-幼儿园
     *
     * @param planId   计划Id
     * @param schoolId 学校Id
     *
     * @return ApiResult<KindergartenVisionReportDTO>
     */
    @GetMapping("/refactor/school/kindergarten")
    public ApiResult<KindergartenVisionReportDTO> refactorKindergartenSchoolVisionReport(Integer planId, Integer schoolId) {
        return ApiResult.success(kindergartenVisionReportService.kindergartenSchoolVisionReport(planId, schoolId));
    }

}
