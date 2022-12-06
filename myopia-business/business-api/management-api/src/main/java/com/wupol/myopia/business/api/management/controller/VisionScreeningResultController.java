package com.wupol.myopia.business.api.management.controller;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.pdf.constant.ExportReportServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.aggregation.student.service.StudentFacade;
import com.wupol.myopia.business.api.management.service.SchoolTemplateService;
import com.wupol.myopia.business.common.utils.constant.ExportTypeConst;
import com.wupol.myopia.business.common.utils.util.FileUtils;
import com.wupol.myopia.business.core.screening.flow.domain.dto.AppStudentCardResponseDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.SchoolResultTemplateExcel;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentCardResponseVO;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/screeningResult")
public class VisionScreeningResultController extends BaseController<VisionScreeningResultService, VisionScreeningResult> {

    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private StudentFacade studentFacade;
    @Autowired
    private ExportStrategy exportStrategy;
    @Autowired
    private SchoolTemplateService schoolTemplateService;

    /**
     * 获取档案卡列表
     *
     * @param schoolId 学校Id
     * @param planId   计划Id
     * @param gradeId  年纪Id
     * @param classId  班级Id
     *
     * @return List<StudentCardResponseVO>
     */
    @GetMapping("/list-result")
    public List<StudentCardResponseVO> listStudentScreeningResult(@RequestParam Integer schoolId,
                                                                  @RequestParam Integer planId, @RequestParam(required = false) Integer resultId,
                                                                  @RequestParam(required = false) Integer gradeId, @RequestParam(required = false) Integer classId,
                                                                  @RequestParam(value = "planStudentIds", required = false) Set<Integer> planStudentIds) {
        // 方便前端模板渲染复用
        if (Objects.nonNull(resultId)) {
            VisionScreeningResult visionScreeningResult = visionScreeningResultService.getById(resultId);
            if (visionScreeningResult == null) {
                throw new BusinessException("无法找到该筛查结果");
            }
            return Lists.newArrayList(studentFacade.getStudentCardResponseDTO(visionScreeningResult));
        }
        ScreeningPlan screeningPlan = screeningPlanService.getById(planId);
        if (screeningPlan == null) {
            throw new BusinessException("无法找到该筛查计划");
        }
        Set<Integer> screeningPlanSchoolStudentIds;
        if (CollectionUtils.isEmpty(planStudentIds)) {
            Integer screeningPlanId = screeningPlan.getId();
            List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = screeningPlanSchoolStudentService.getByPlanIdAndSchoolIdAndGradeIdAndClassId(screeningPlanId, schoolId,
                    gradeId, classId);
            if (CollectionUtils.isEmpty(screeningPlanSchoolStudents)) {
                return new ArrayList<>();
            }
            screeningPlanSchoolStudentIds = screeningPlanSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getId).collect(Collectors.toSet());
        } else {
            screeningPlanSchoolStudentIds = planStudentIds;
        }
        List<VisionScreeningResult> visionScreeningResults = visionScreeningResultService.getByScreeningPlanSchoolStudentIds(screeningPlanSchoolStudentIds,false);
        return studentFacade.generateBatchStudentCard(visionScreeningResults);
    }

    /**
     * 获取学生筛查计划档案卡
     *
     * @param planStudentId
     *
     * @return
     */
    @GetMapping("/screening/planStudent/card/{planStudentId}")
    public AppStudentCardResponseDTO getResultByPlanStudentId(@PathVariable("planStudentId") Integer planStudentId) {
        return studentFacade.getCardDetailByPlanStudentId(planStudentId);
    }

    /**
     * @Description: 导出文件
     * @Param: [筛查计划ID, 筛查机构ID, 学校ID, 年级ID, 班级ID，行政区域ID]
     * @return: void
     * @Author: 钓猫的小鱼
     * @Date: 2021/12/29
     */
    @GetMapping("/plan/export/schoolInfo")
    public ApiResult getScreeningPlanExportDoAndSync(Integer screeningPlanId, @RequestParam(required = false) Integer screeningOrgId,
                                                     @RequestParam(required = false) Integer schoolId,
                                                     @RequestParam(required = false) Integer gradeId,
                                                     @RequestParam(required = false) Integer classId,
                                                     @RequestParam(required = false) Integer districtId,
                                                     @RequestParam Integer type, Integer screeningNoticeId,
                                                     @RequestParam(required = false) Boolean isKindergarten) throws IOException {
        ExportCondition exportCondition = new ExportCondition()
                .setPlanId(screeningPlanId)
                .setScreeningOrgId(screeningOrgId)
                .setSchoolId(schoolId)
                .setGradeId(gradeId)
                .setClassId(classId)
                .setDistrictId(districtId)
                .setNotificationId(screeningNoticeId)
                .setExportType(type)
                .setIsKindergarten(isKindergarten)
                .setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId());

        // 班级同步导出
        if (ExportTypeConst.CLASS.equals(type)) {
            String path = exportStrategy.syncExport(exportCondition, ExportReportServiceNameConstant.EXPORT_PLAN_STUDENT_DATA_EXCEL_SERVICE);
            return ApiResult.success(path);
        }
        exportStrategy.doExport(exportCondition, ExportReportServiceNameConstant.EXPORT_PLAN_STUDENT_DATA_EXCEL_SERVICE);
        return ApiResult.success();
    }

    /**
     * 导出学校筛查模板
     */
    @GetMapping("/school/template/export")
    public void exportSchoolResultExcelTemplate(Integer screeningPlanId, Integer schoolId) throws IOException {
        ExportCondition exportCondition = new ExportCondition()
                .setPlanId(screeningPlanId)
                .setSchoolId(schoolId)
                .setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId());
        exportStrategy.doExport(exportCondition, ExportReportServiceNameConstant.EXPORT_SCHOOL_RESULT_TEMPLATE_EXCEL_SERVICE);
    }

    /**
     * 导入学校筛查模板
     */
    @PostMapping("/school/template/import")
    public void importSchoolResultExcelTemplate(MultipartFile file) {
//        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        List<Map<Integer, String>> listMap = FileUtils.readExcel(file);
        if (CollectionUtils.isEmpty(listMap)) {
            throw new BusinessException("数据为空");
        }
        List<SchoolResultTemplateExcel> schoolResultTemplateExcels = schoolTemplateService.parseExcelData(listMap);
        schoolTemplateService.importSchoolScreeningData(schoolResultTemplateExcels, 16);
    }

}
