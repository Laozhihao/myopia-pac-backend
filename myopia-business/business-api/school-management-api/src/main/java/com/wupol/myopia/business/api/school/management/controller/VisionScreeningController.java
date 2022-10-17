package com.wupol.myopia.business.api.school.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.domain.PdfResponseDTO;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExportExcelServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.constant.ExportReportServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.aggregation.screening.domain.dto.GeneratorPdfDTO;
import com.wupol.myopia.business.aggregation.screening.domain.dto.SchoolScreeningPlanDTO;
import com.wupol.myopia.business.aggregation.screening.domain.vos.SchoolGradeVO;
import com.wupol.myopia.business.aggregation.screening.domain.vos.ScreeningPlanVO;
import com.wupol.myopia.business.aggregation.screening.domain.vos.StudentScreeningDetailVO;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningExportService;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningPlanSchoolStudentFacadeService;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningPlanStudentBizService;
import com.wupol.myopia.business.api.school.management.domain.dto.AddScreeningStudentDTO;
import com.wupol.myopia.business.api.school.management.domain.dto.ScreeningEndTimeDTO;
import com.wupol.myopia.business.api.school.management.domain.dto.StudentListDTO;
import com.wupol.myopia.business.api.school.management.domain.vo.SchoolStatistic;
import com.wupol.myopia.business.api.school.management.domain.vo.ScreeningStudentVO;
import com.wupol.myopia.business.api.school.management.facade.SchoolScreeningStatisticFacade;
import com.wupol.myopia.business.api.school.management.service.VisionScreeningService;
import com.wupol.myopia.business.common.utils.constant.ExportTypeConst;
import com.wupol.myopia.business.common.utils.domain.model.NotificationConfig;
import com.wupol.myopia.business.common.utils.domain.model.ResultNoticeConfig;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
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
    private ScreeningPlanSchoolStudentFacadeService screeningPlanSchoolStudentFacadeService;
    @Resource
    private ScreeningExportService screeningExportService;
    @Resource
    private SchoolService schoolService;
    @Resource
    private ExportStrategy exportStrategy;
    @Resource
    private ScreeningPlanStudentBizService screeningPlanStudentBizService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Resource
    private SchoolScreeningStatisticFacade schoolScreeningStatisticFacade;

    /**
     * 获取学校计划
     *
     * @param screeningPlanListDTO 筛查计划列表查询对象
     * @return IPage<ScreeningListResponseDTO>
     */
    @GetMapping("list")
    public IPage<ScreeningListResponseDTO> getList(ScreeningPlanListDTO screeningPlanListDTO) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return visionScreeningService.getList(screeningPlanListDTO, currentUser.getOrgId());
    }

    /**
     * 获取结果统计分析
     *
     * @param screeningPlanId 筛查计划ID
     * @param type tab类型(8:幼儿园，0:小学及以上)
     */
    @GetMapping("{screeningPlanId}")
    public SchoolStatistic getSchoolStatistic(@PathVariable("screeningPlanId") Integer screeningPlanId, @RequestParam Integer type) {
        return visionScreeningService.getSchoolStatistic(screeningPlanId,CurrentUserUtil.getCurrentUser().getOrgId(),type);
    }

    /**
     * 获取计划学校的年级情况
     *
     * @param screeningPlanId 计划ID
     * @return List<SchoolGradeVo>
     */
    @GetMapping("grades/{screeningPlanId}")
    public List<SchoolGradeVO> queryGradesInfo(@PathVariable Integer screeningPlanId,Boolean isData) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return screeningPlanSchoolStudentFacadeService.getSchoolGradeVoByPlanIdAndSchoolId(screeningPlanId, currentUser.getOrgId(),isData);
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
    public ScreeningPlanVO getPlanInfo(@PathVariable("screeningPlanId") Integer screeningPlanId) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return schoolScreeningStatisticFacade.getPlanInfo(screeningPlanId,currentUser);

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
     * //TODO 当前这个二维码导出，在其他地方有使用，考虑是否删除
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
        visionScreeningService.getScreeningPlanExportData(planId,currentUser);
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
                .setExportType(ExportTypeConst.SCHOOL)
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
     * 异步导出学生报告
     *
     * @param generatorPdfDTO
     */
    @GetMapping("screeningNoticeResult/asyncGeneratorPDF")
    public void asyncGeneratorPDF(@Valid GeneratorPdfDTO generatorPdfDTO) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        generatorPdfDTO.setIsSchoolClient(Boolean.TRUE);
        generatorPdfDTO.setUserId(user.getId());
        generatorPdfDTO.setSchoolId(user.getOrgId());
        screeningPlanStudentBizService.asyncGeneratorPDF(generatorPdfDTO);
    }

    /**
     * 同步导出学生报告
     *
     * @param generatorPdfDTO
     */
    @GetMapping("screeningNoticeResult/syncGeneratorPDF")
    public PdfResponseDTO syncGeneratorPDF(GeneratorPdfDTO generatorPdfDTO) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        generatorPdfDTO.setSchoolId(user.getOrgId());
        generatorPdfDTO.setIsSchoolClient(Boolean.TRUE);
        generatorPdfDTO.setUserId(user.getId());
        return screeningPlanStudentBizService.syncGeneratorPDF(generatorPdfDTO);
    }

    /**
     * 通过条件获取筛查学生
     *
     * @param generatorPdfDTO
     * @return List<ScreeningStudentDTO>
     */
    @GetMapping("screeningNoticeResult/list")
    public List<ScreeningStudentDTO> getScreeningNoticeResultLists(@Valid GeneratorPdfDTO generatorPdfDTO) {
        return screeningPlanStudentBizService.getScreeningStudentDTOS(generatorPdfDTO);
    }

    /**
     * 学生筛查数据
     *
     * @param resultId
     * @return
     */
    @GetMapping("/studentData")
    public VisionScreeningResult studentData(@RequestParam Integer resultId) {
        return visionScreeningResultService.getById(resultId);
    }

    /**
     * 创建/编辑筛查计划
     * @param schoolScreeningPlanDTO 创建/编辑筛查计划对象
     */
    @PostMapping("/save")
    public void saveScreeningPlan(@RequestBody @Valid SchoolScreeningPlanDTO schoolScreeningPlanDTO){
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        visionScreeningService.saveScreeningPlan(schoolScreeningPlanDTO,currentUser);
    }

    /**
     * 删除筛查计划
     * @param screeningPlanId 筛查计划ID
     */
    @DeleteMapping("/delete/{screeningPlanId}")
    public void deleteScreeningPlan(@PathVariable("screeningPlanId") Integer screeningPlanId){
        // 判断是否已发布
        visionScreeningService.validateExistWithReleaseStatus(screeningPlanId);
        visionScreeningService.deleteScreeningPlan(screeningPlanId);
    }

    /**
     * 发布筛查计划
     * @param screeningPlanId 筛查计划ID
     */
    @PutMapping("/release/{screeningPlanId}")
    public void releaseScreeningPlan(@PathVariable("screeningPlanId") Integer screeningPlanId){
        visionScreeningService.releaseScreeningPlan(screeningPlanId);
    }

    /**
     * 获取筛查学生列表
     * @param studentListDTO 学生查询条件对象
     */
    @GetMapping("/student/list")
    public ScreeningStudentVO studentList(@Valid StudentListDTO studentListDTO){
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        studentListDTO.setSchoolId(currentUser.getOrgId());
        return visionScreeningService.studentList(studentListDTO);
    }

    /**
     * 新增筛查学生
     * @param addScreeningStudentDTO 新增筛查学校对象
     */
    @PostMapping("/addScreeningStudent")
    public void addScreeningStudent(@Valid AddScreeningStudentDTO addScreeningStudentDTO){
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        addScreeningStudentDTO.setSchoolId(currentUser.getOrgId());
        visionScreeningService.addScreeningStudent(addScreeningStudentDTO);
    }

    /**
     * 学生筛查详情
     * @param screeningPlanId 筛查计划ID
     * @param screeningPlanStudentId 筛查计划学生ID
     */
    @GetMapping("/studentScreeningDetail")
    public StudentScreeningDetailVO studentScreeningDetail(@RequestParam Integer screeningPlanId,
                                                           @RequestParam Integer screeningPlanStudentId){
        return visionScreeningService.studentScreeningDetail(screeningPlanId,screeningPlanStudentId);
    }

    /**
     * 增加筛查时间
     * @param screeningEndTimeDTO 筛查结束时间参数
     */
    @PostMapping("/increased/screeningTime")
    public void updateScreeningEndTime (@RequestBody @Valid ScreeningEndTimeDTO screeningEndTimeDTO) {
        visionScreeningService.updateScreeningEndTime(screeningEndTimeDTO);
    }

}
