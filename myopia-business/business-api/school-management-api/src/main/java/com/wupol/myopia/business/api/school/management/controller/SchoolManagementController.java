package com.wupol.myopia.business.api.school.management.controller;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.pdf.constant.ExportReportServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningExportService;
import com.wupol.myopia.business.aggregation.student.service.SchoolFacade;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.GradeCode;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeItemsDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolResponseDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学校端-学校管理
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/school/management")
public class SchoolManagementController {
    @Autowired
    private ExportStrategy exportStrategy;
    @Resource
    private SchoolClassService schoolClassService;
    @Resource
    private SchoolGradeService schoolGradeService;
    @Resource
    private SchoolFacade schoolFacade;
    @Autowired
    private ScreeningExportService screeningExportService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    /**
     * 保存班级
     *
     * @param schoolClass 班级实体
     * @return 新增数量
     */
    @PostMapping()
    public Integer saveGrade(@RequestBody @Valid SchoolClass schoolClass) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        schoolClass.setCreateUserId(user.getId());
        schoolClass.setSchoolId(user.getOrgId());
        return schoolClassService.saveClass(schoolClass);
    }

    /**
     * 删除班级
     *
     * @param id 班级ID
     * @return 删除数量
     */
    @DeleteMapping("{id}")
    public Integer deletedClass(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return schoolClassService.deletedClass(id, user.getId());
    }

    /**
     * 更新班级
     *
     * @param schoolClass 班级实体
     * @return 班级实体
     */
    @PutMapping()
    public SchoolClass updateClass(@RequestBody @Valid SchoolClass schoolClass) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        schoolClass.setCreateUserId(user.getId());
        schoolClass.setSchoolId(user.getOrgId());
        return schoolClassService.updateClass(schoolClass);
    }

    /**
     * 获取年级ID获取班级列表
     *
     * @param gradeId 年级ID
     * @return 班级列表
     */
    @GetMapping("all")
    public List<SchoolClass> getAllClassList(Integer gradeId) {
        if (null == gradeId) {
            throw new BusinessException("年级ID不能为空");
        }
        return schoolClassService.getByGradeId(gradeId);
    }

    /**
     * 更新年级
     *
     * @param schoolGrade 年级实体
     * @return 新增个数
     */
    @PostMapping("grade")
    public Integer saveGrade(@RequestBody @Valid SchoolGrade schoolGrade) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        schoolGrade.setCreateUserId(user.getId());
        schoolGrade.setSchoolId(user.getOrgId());
        return schoolGradeService.saveGrade(schoolGrade);
    }

    /**
     * 删除年级
     *
     * @param id 年级ID
     * @return 删除个数
     */
    @DeleteMapping("/grade/{id}")
    public Integer deletedGrade(@PathVariable("id") Integer id) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return schoolGradeService.deletedGrade(id, currentUser);
    }

    /**
     * 年级列表
     *
     * @param pageRequest 分页请求
     * @return 年级列表
     */
    @GetMapping("grade/list")
    public IPage<SchoolGradeItemsDTO> getGradeList(PageRequest pageRequest) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return schoolGradeService.getGradeList(pageRequest, currentUser.getOrgId());
    }

    /**
     * 获取年级列表（不分页）
     *
     * @return 年级列表
     */
    @GetMapping("grade/all")
    public List<SchoolGradeItemsDTO> getAllGradeList() {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return schoolGradeService.getAllGradeList(currentUser.getOrgId());
    }

    /**
     * 获取年级编码
     *
     * @return 年级编码
     */
    @GetMapping("/grade/getGradeCode")
    public List<GradeCode> getGradeCode() {
        return GradeCodeEnum.getGradeCodeList();
    }

    /**
     * 更新年级
     *
     * @param schoolGrade 年级实体
     * @return 年级实体
     */
    @PutMapping("grade")
    public SchoolGrade updateGrade(@RequestBody @Valid SchoolGrade schoolGrade) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        schoolGrade.setCreateUserId(user.getId());
        schoolGrade.setSchoolId(user.getOrgId());
        return schoolGradeService.updateGrade(schoolGrade);
    }


    /**
     * 通过ID获取学校详情
     *
     * @param id 学校ID
     * @return 学校实体
     */
    @GetMapping("/school/{id}")
    public SchoolResponseDTO getSchoolDetail(@PathVariable("id") Integer id) {
        return schoolFacade.getBySchoolId(id, true);
    }


    /**
     * 更新学校
     *
     * @param school 学校实体
     * @return 学校实体
     */
    @PutMapping("/school")
    public SchoolResponseDTO updateSchool(@RequestBody @Valid School school) {
        return schoolFacade.updateSchool(school);
    }

    /**
     * 获取计划学校-年级-班级 下的学生
     * @param screeningPlanId 筛查计划ID
     * @param schoolId  学校ID
     * @param gradeId 年级ID
     * @param classId 班级ID
     * @return
     */
    @GetMapping("/screeningPlan/students/{screeningPlanId}/{schoolId}/{gradeId}/{classId}")
    public List<ScreeningPlanSchoolStudent> queryGradesInfo(@PathVariable Integer screeningPlanId, @PathVariable Integer schoolId,
                                                            @PathVariable Integer gradeId, @PathVariable Integer classId) {
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = screeningPlanSchoolStudentService.getByPlanIdAndSchoolIdAndGradeIdAndClassId(screeningPlanId, schoolId,
                gradeId, classId);
        return screeningPlanSchoolStudents;
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
                                                       @NotNull(message = "TypeID不能为空") Integer type) throws IOException {

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
            return ApiResult.success(exportStrategy.syncExport(exportCondition, ExportReportServiceNameConstant.EXPORT_QRCODE_SCREENIN_SERVICE));
        }
        exportStrategy.doExport(exportCondition, ExportReportServiceNameConstant.EXPORT_QRCODE_SCREENIN_SERVICE);
        return ApiResult.success();
    }

}
