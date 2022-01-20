package com.wupol.myopia.business.api.school.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.domain.PdfResponseDTO;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.excel.ExcelFacade;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExportExcelServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.constant.ExportReportServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.aggregation.screening.domain.vos.SchoolGradeVO;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningExportService;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningPlanSchoolStudentFacadeService;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningPlanStudentBizService;
import com.wupol.myopia.business.api.school.management.service.VisionScreeningService;
import com.wupol.myopia.business.common.utils.domain.model.NotificationConfig;
import com.wupol.myopia.business.common.utils.domain.model.ResultNoticeConfig;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.interfaces.HasName;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.stat.domain.model.SchoolVisionStatistic;
import com.wupol.myopia.business.core.stat.service.SchoolVisionStatisticService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 视力筛查
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/school/vision/screening")
public class VisionScreeningController {

    @Resource
    private VisionScreeningService visionScreeningService;

    @Resource
    private SchoolVisionStatisticService schoolVisionStatisticService;

    @Resource
    private ScreeningPlanSchoolStudentFacadeService screeningPlanSchoolStudentFacadeService;

    @Resource
    private ScreeningPlanService screeningPlanService;

    @Resource
    private ScreeningExportService screeningExportService;

    @Resource
    private ExcelFacade excelFacade;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private SchoolService schoolService;

    @Resource
    private StatConclusionService statConclusionService;

    @Resource
    private DistrictService districtService;

    @Resource
    private ExportStrategy exportStrategy;

    @Resource
    private ScreeningPlanStudentBizService screeningPlanStudentBizService;

    /**
     * 获取学校计划
     *
     * @param pageRequest 分页请求
     * @return IPage<ScreeningListResponseDTO>
     */
    @GetMapping("list")
    public IPage<ScreeningListResponseDTO> getList(PageRequest pageRequest) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return visionScreeningService.getList(pageRequest, currentUser.getOrgId());
    }

    /**
     * 获取结果统计分析
     *
     * @param schoolStatisticId 结果统计
     * @return SchoolVisionStatistic
     */
    @GetMapping("{schoolStatisticId}")
    public SchoolVisionStatistic getSchoolStatistic(@PathVariable("schoolStatisticId") Integer schoolStatisticId) {
        return schoolVisionStatisticService.getById(schoolStatisticId);
    }

    /**
     * 获取计划学校的年级情况
     *
     * @param screeningPlanId 计划ID
     * @return List<SchoolGradeVo>
     */
    @GetMapping("grades/{screeningPlanId}")
    public List<SchoolGradeVO> queryGradesInfo(@PathVariable Integer screeningPlanId) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return screeningPlanSchoolStudentFacadeService.getSchoolGradeVoByPlanIdAndSchoolId(screeningPlanId, currentUser.getOrgId());
    }

    /**
     * 获取学生跟踪预警列表
     *
     * @param pageRequest 分页请求
     * @param requestDTO  入参
     * @return IPage<StudentTrackWarningResponseDTO>
     */
    @GetMapping("statStudents/list")
    public IPage<StudentTrackWarningResponseDTO> queryStudentInfos(PageRequest pageRequest, @Valid StudentTrackWarningRequestDTO requestDTO) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return visionScreeningService.getTrackList(pageRequest, requestDTO, currentUser.getOrgId());
    }

    /**
     * 获取筛查计划信息
     *
     * @param screeningPlanId 筛查计划Id
     * @return ScreeningPlan
     */
    @GetMapping("/plan/{screeningPlanId}")
    public ScreeningPlan getPlanInfo(@PathVariable("screeningPlanId") Integer screeningPlanId) {
        return screeningPlanService.getById(screeningPlanId);
    }

    /**
     * 导出筛查计划的学生告知书
     *
     * @param schoolClassInfo 参与筛查计划的学生
     * @return PDF的URL
     */
    @GetMapping("/export/notice")
    public Map<String, String> downloadNoticeFile(@Valid ScreeningPlanSchoolStudent schoolClassInfo) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return screeningExportService.getNoticeFile(schoolClassInfo, currentUser.getOrgId());
    }

    /**
     * 导出筛查计划的学生二维码信息
     *
     * @param schoolClassInfo 参与筛查计划的学生
     * @param type            1-二维码 2-VS666 3-学生编码二维码
     * @return pdf的URL
     */
    @GetMapping("/export/QRCode")
    public Map<String, String> downloadQRCodeFile(@Valid ScreeningPlanSchoolStudent schoolClassInfo, Integer type) {
        return screeningExportService.getQrCodeFile(schoolClassInfo, type);
    }

    /**
     * 导出筛查数据
     *
     * @param planId 筛查计划Id
     * @return ApiResult
     */
    @GetMapping("/plan/export")
    public Object getScreeningPlanExportData(Integer planId) throws IOException, UtilException {

        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        Integer schoolId = currentUser.getOrgId();

        List<StatConclusionExportDTO> statConclusionExportDTOs;

        // 获取文件需显示的名称的学校前缀
        String exportFileNamePrefix = checkNotNullAndGetName(schoolService.getById(schoolId));
        statConclusionExportDTOs = statConclusionService.getExportVoByScreeningPlanIdAndSchoolId(planId, schoolId);
        if (CollectionUtils.isEmpty(statConclusionExportDTOs)) {
            throw new BusinessException("暂无筛查数据，无法导出");
        }
        statConclusionExportDTOs.forEach(vo -> vo.setAddress(districtService.getAddressDetails(vo.getProvinceCode(), vo.getCityCode(), vo.getAreaCode(), vo.getTownCode(), vo.getAddress())));
        String key = String.format(RedisConstant.FILE_EXPORT_PLAN_DATA, planId, 0, schoolId, currentUser.getId());
        checkIsExport(key);
        // 获取文件需显示的名称
        excelFacade.generateVisionScreeningResult(currentUser.getId(), statConclusionExportDTOs, true, exportFileNamePrefix, key);
        return ApiResult.success();
    }

    /**
     * 导出学校的筛查报告
     *
     * @param screeningNoticeId 筛查通知Id
     * @param planId            筛查计划Id
     **/
    @GetMapping("/school/export")
    public void exportSchoolReport(Integer screeningNoticeId, Integer planId) throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        if (Objects.isNull(screeningNoticeId) && Objects.isNull(planId)) {
            throw new BusinessException("筛查通知ID或者筛查计划ID不能为空");
        }
        ExportCondition exportCondition = new ExportCondition()
                .setNotificationId(screeningNoticeId)
                .setPlanId(planId)
                .setSchoolId(currentUser.getOrgId())
                .setApplyExportFileUserId(currentUser.getId());
        if (Objects.nonNull(screeningNoticeId) && screeningNoticeId == 0) {
            exportCondition.setNotificationId(null);
        }
        exportStrategy.doExport(exportCondition, ExportReportServiceNameConstant.SCHOOL_SCREENING_REPORT_SERVICE);
    }

    /**
     * 导出指定计划下的单个学校的学生的预计跟踪档案
     *
     * @param planId         筛查计划Id
     * @param screeningOrgId 筛查机构Id
     **/
    @GetMapping("/export/student/warning/archive")
    public void exportStudentWarningArchive(@NotNull(message = "planId不能为空") Integer planId,
                                            @NotNull(message = "screeningOrgId不能为空") Integer screeningOrgId) throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        exportStrategy.doExport(new ExportCondition().setApplyExportFileUserId(user.getId()).setPlanId(planId).setSchoolId(user.getOrgId()).setScreeningOrgId(screeningOrgId),
                ExportExcelServiceNameConstant.STUDENT_WARNING_ARCHIVE_EXCEL_SERVICE);
    }

    /**
     * 更新学校
     *
     * @param notificationConfig 告知书配置
     */
    @PutMapping("update")
    @Transactional(rollbackFor = Exception.class)
    public Object updateSchool(@RequestBody NotificationConfig notificationConfig) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        School school = schoolService.getBySchoolId(currentUser.getOrgId());
        school.setNotificationConfig(notificationConfig);
        schoolService.updateById(school);
        return ApiResult.success();
    }

    /**
     * 更新结果通知
     *
     * @param id                 学校Id
     * @param resultNoticeConfig 结果通知
     */
    @PutMapping("/update/resultNoticeConfig/{id}")
    public void updateResultNoticeConfig(@PathVariable("id") @NotNull(message = "学校Id不能为空") Integer id,
                                         @RequestBody ResultNoticeConfig resultNoticeConfig) {
        schoolService.updateResultNoticeConfig(id, resultNoticeConfig);
    }

    /**
     * 判空并获取名称
     *
     * @param object 类型
     * @return 名称
     */
    private <T extends HasName> String checkNotNullAndGetName(T object) {
        if (Objects.isNull(object)) {
            throw new BusinessException(String.format("未找到该%s", "学校"));
        }
        return object.getName();
    }

    /**
     * 是否正在导出
     *
     * @param key Key
     */
    private void checkIsExport(String key) {
        Object o = redisUtil.get(key);
        if (Objects.nonNull(o)) {
            throw new BusinessException("正在导出中，请勿重复导出");
        }
        redisUtil.set(key, 1, 60 * 60 * 24);
    }

    /**
     * 通过条件获取筛查学生
     *
     * @param planId         计划Id
     * @param schoolId       学校Id
     * @param gradeId        年级Id
     * @param classId        班级Id
     * @param orgId          筛查机构Id
     * @param planStudentIds 筛查学生Id
     * @param isSchoolClient 是否学校端
     * @return List<ScreeningStudentDTO>
     */
    @GetMapping("screeningNoticeResult")
    public List<ScreeningStudentDTO> getScreeningNoticeResultStudent(@NotBlank(message = "计划Id不能为空") Integer planId,
                                                                     Integer schoolId, Integer gradeId, Integer classId, Integer orgId,
                                                                     String planStudentIds, @NotBlank(message = "查询类型不能为空") Boolean isSchoolClient,
                                                                     String planStudentName) {
        return screeningPlanStudentBizService.getScreeningNoticeResultStudent(planId, schoolId, gradeId, classId, orgId, planStudentIds, isSchoolClient, planStudentName);
    }

    /**
     * 异步导出学生报告
     *
     * @param planId           计划Id
     * @param gradeId          年级Id
     * @param classId          班级Id
     * @param orgId            筛查机构Id
     * @param planStudentIdStr 筛查学生Ids
     */
    @GetMapping("screeningNoticeResult/asyncGeneratorPDF")
    public void asyncGeneratorPDF(Integer planId, Integer gradeId, Integer classId, Integer orgId, String planStudentIdStr) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        screeningPlanStudentBizService.asyncGeneratorPDF(planId, user.getOrgId(), gradeId, classId, orgId, planStudentIdStr, true, user.getId());
    }

    /**
     * 同步导出学生报告
     *
     * @param planId           计划Id
     * @param gradeId          年级Id
     * @param classId          班级Id
     * @param orgId            筛查机构Id
     * @param planStudentIdStr 筛查学生Ids
     */
    @GetMapping("screeningNoticeResult/syncGeneratorPDF")
    public PdfResponseDTO syncGeneratorPDF(Integer planId, Integer gradeId, Integer classId, Integer orgId, String planStudentIdStr) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return screeningPlanStudentBizService.syncGeneratorPDF(planId, user.getOrgId(), gradeId, classId, orgId, planStudentIdStr, true, user.getId());
    }

    /**
     * 通过条件获取筛查学生
     *
     * @param planId           计划Id
     * @param schoolId         学校Id
     * @param gradeId          年级Id
     * @param classId          班级Id
     * @param planStudentIdStr 筛查学生Ids
     * @param planStudentName  筛查学生名称
     * @return List<ScreeningStudentDTO>
     */
    @GetMapping("screeningNoticeResult/list")
    public List<ScreeningStudentDTO> getScreeningNoticeResultLists(@NotBlank(message = "计划Id不能为空") Integer planId, Integer schoolId, Integer gradeId, Integer classId, String planStudentIdStr, String planStudentName) {
        return screeningPlanStudentBizService.getScreeningStudentDTOS(planId, schoolId, gradeId, classId, planStudentIdStr, planStudentName);
    }
}
