package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExportExcelServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.excel.imports.SchoolStudentExcelImportService;
import com.wupol.myopia.business.aggregation.export.excel.imports.StudentExcelImportService;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.aggregation.hospital.domain.dto.StudentVisitReportResponseDTO;
import com.wupol.myopia.business.aggregation.hospital.service.MedicalReportBizService;
import com.wupol.myopia.business.aggregation.student.domain.vo.StudentWarningArchiveVO;
import com.wupol.myopia.business.aggregation.student.service.SchoolStudentFacade;
import com.wupol.myopia.business.aggregation.student.service.StudentFacade;
import com.wupol.myopia.business.api.management.domain.dto.SchoolStudentDTO;
import com.wupol.myopia.business.api.management.service.StudentBizService;
import com.wupol.myopia.business.common.utils.constant.VisionLabels;
import com.wupol.myopia.business.common.utils.constant.VisionLabelsEnum;
import com.wupol.myopia.business.common.utils.domain.dto.Nation;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.hospital.domain.dos.ReportAndRecordDO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolStudentQueryDTO;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.domain.dto.StudentQueryDTO;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.domain.vo.SchoolStudentQuerySelectVO;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.domain.vo.SchoolStudentListVO;
import com.wupol.myopia.business.core.school.management.domain.vo.SchoolStudentVO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentScreeningResultItemsDTO;
import com.wupol.myopia.business.core.screening.flow.domain.vo.ReScreeningCardVO;
import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentCardResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * 学生Controller
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/student")
public class StudentController {

    @Autowired
    private StudentBizService studentBizService;

    @Autowired
    private ExportStrategy exportStrategy;

    @Autowired
    private MedicalReportBizService medicalReportBizService;

    @Autowired
    private StudentFacade studentFacade;

    @Autowired
    private StudentExcelImportService studentExcelImportService;
    @Autowired
    private SchoolStudentFacade schoolStudentFacade;
    @Autowired
    private SchoolStudentExcelImportService schoolStudentExcelImportService;
    /**
     * 新增学生
     *
     * @param student 学生实体
     * @return 新增数量
     */
    @PostMapping()
    public Integer saveStudent(@RequestBody @Valid Student student) {
        student.checkStudentInfo();
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        student.setCreateUserId(user.getId());
        return studentFacade.saveStudentAndSchoolStudent(student);
    }

    /**
     * 更新学生
     *
     * @param student 学生实体
     * @return 学生实体
     */
    @PutMapping()
    public StudentDTO updateStudent(@RequestBody @Valid Student student) {
        student.checkStudentInfo();
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        student.setCreateUserId(user.getId());
        return studentBizService.updateStudentReturnCountInfo(student, user);
    }

    /**
     * 删除学生
     *
     * @param id 学生ID
     * @return 是否删除成功
     */
    @DeleteMapping("{id}")
    public Boolean deletedStudent(@PathVariable("id") Integer id) {
        return studentBizService.deletedStudent(id);
    }

    /**
     * 获取学生详情
     *
     * @param id 学生ID
     * @return 学生实体 {@link StudentDTO}
     */
    @GetMapping("{id}")
    public StudentDTO getStudent(@PathVariable("id") Integer id) {
        return studentFacade.getStudentById(id);
    }

    /**
     * 获取学生列表
     *
     * @param pageRequest  分页查询
     * @param studentQuery 请求条件
     * @return 学生列表
     */
    @GetMapping("list")
    public IPage<StudentDTO> getStudentsList(PageRequest pageRequest, StudentQueryDTO studentQuery) {
        return studentBizService.getStudentLists(pageRequest, studentQuery);
    }



    /**
     * 导出学生列表
     *
     * @param schoolId 学校ID
     * @param gradeId  年级ID
     * @param clientId  客户端ID
     */
    @GetMapping("/export")
    public void getStudentExportData(Integer schoolId, Integer gradeId,Integer clientId) throws IOException {
        Assert.notNull(schoolId, "学校id不能为空");
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        ExportCondition exportCondition = new ExportCondition()
                .setApplyExportFileUserId(user.getId())
                .setSchoolId(schoolId)
                .setGradeId(gradeId);
        if (Objects.equals(clientId, SystemCode.SCHOOL_CLIENT.getCode())){
            //管理端导出学校学生
            exportStrategy.doExport(exportCondition, ExportExcelServiceNameConstant.SCHOOL_STUDENT_EXCEL_SERVICE);
            return;
        }
        exportStrategy.doExport(exportCondition, ExportExcelServiceNameConstant.STUDENT_EXCEL_SERVICE);
    }

    /**
     * 导入学生列表
     *
     * @param file 导入文件
     */
    @PostMapping("/import")
    public void importStudent(MultipartFile file, Integer schoolId,Integer client) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        if (Objects.equals(client, SystemCode.SCHOOL_CLIENT.getCode())){
            schoolStudentExcelImportService.importSchoolStudent(currentUser.getId(),file,schoolId);
            return;
        }
        studentExcelImportService.importStudent(currentUser.getId(), file, schoolId);
    }

    /**
     * 获取视力标签
     *
     * @return 视力标签
     */
    @GetMapping("labels")
    public List<VisionLabels> getVisionLabels() {
        return VisionLabelsEnum.getVisionLabels();
    }

    /**
     * 获取民族列表
     *
     * @return 民族列表
     */
    @GetMapping("nation")
    public List<Nation> getNationLists() {
        return studentFacade.getNationLists();
    }


    /**
     * 获取学生筛查档案
     *
     * @param id 学生ID
     * @return 学生筛查档案
     */
    @GetMapping("/screening")
    public IPage<StudentScreeningResultItemsDTO> getScreeningList(PageRequest pageRequest, @NotNull(message = "学生Id不能为空") Integer id) {
        return studentFacade.getScreeningList(pageRequest, id, CurrentUserUtil.getCurrentUser());
    }

    /**
     * 获取学生复测卡
     * @param planStudentId 计划学生ID
     * @param planId 计划ID
     * @return 复测卡
     */
    @GetMapping("/screeningResult")
    public ReScreeningCardVO getRetestResult(@NotNull(message = "planStudentId不能为空") Integer planStudentId, @NotNull(message = "planId不能为空") Integer planId ) {
        return studentFacade.getRetestResult(planStudentId, planId);
    }

    /**
     * 获取学生档案卡
     *
     * @param resultId 筛查结果ID
     * @return 学生档案卡
     */
    @GetMapping("/screening/card/{resultId}")
    public StudentCardResponseVO getCardDetails(@PathVariable("resultId") Integer resultId) {
        return studentFacade.getCardDetail(resultId);
    }

    /**
     * 获取就诊列表
     *
     * @param pageRequest 分页请求
     * @param studentId   学生Id
     * @param hospitalId  医院Id
     * @return List<MedicalReportDO>
     */
    @GetMapping("/report/list")
    public IPage<ReportAndRecordDO> getReportList(PageRequest pageRequest, @NotNull(message = "学生Id不能为空") Integer studentId, Integer hospitalId) {
        return studentBizService.getReportList(pageRequest, studentId, CurrentUserUtil.getCurrentUser(), hospitalId);
    }

    /**
     * 就诊卡（报告详情）
     *
     * @param reportId 报告Id
     * @return StudentVisitReportResponseDTO
     */
    @GetMapping("/report/detail/{reportId}")
    public StudentVisitReportResponseDTO getReportDetail(@PathVariable("reportId") Integer reportId) {
        return medicalReportBizService.getStudentVisitReport(reportId);
    }

    /**
     * 获取学生预警跟踪档案
     *
     * @param studentId 学生ID
     * @return java.util.List<com.wupol.myopia.business.api.management.domain.vo.StudentWarningArchiveVO>
     **/
    @GetMapping("/warning/archive/{studentId}")
    public IPage<StudentWarningArchiveVO> getStudentWarningArchive(PageRequest pageRequest,@PathVariable("studentId") Integer studentId) {
        return studentFacade.getStudentWarningArchive(pageRequest,studentId);
    }

    /**
     * 获取学校学生查询条件下拉框值（多端管理-学校管理-学生管理）
     * @param schoolId
     */
    @GetMapping("/selectValue")
    public SchoolStudentQuerySelectVO getSelectValue(@RequestParam Integer schoolId){
        Assert.notNull(schoolId,"学校ID不能为空");
        return schoolStudentFacade.getSelectValue(schoolId);
    }

    /**
     * 获取学校学生列表（多端管理-学校管理-学生管理）
     *
     * @param pageRequest  分页查询
     * @param studentQuery 请求条件
     * @return 学生列表
     */
    @GetMapping("/schoolList")
    public IPage<SchoolStudentListVO> getSchoolStudentsList(PageRequest pageRequest, SchoolStudentQueryDTO studentQuery) {
        return studentBizService.getSchoolStudentList(pageRequest, studentQuery);
    }

    /**
     * 获取筛查记录
     *
     * @param id 学校学生Id
     * @return StudentScreeningResultResponseDTO
     */
    @GetMapping("/screening/list/{id}")
    public IPage<StudentScreeningResultItemsDTO> screeningList(PageRequest pageRequest, @PathVariable("id") Integer id) {
        return studentFacade.getSchoolScreeningList(pageRequest, id,CurrentUserUtil.getCurrentUser(),SystemCode.MANAGEMENT_CLIENT.getCode());
    }

    /**
     * 获取学校学生详情
     *
     * @param id 学校学生ID
     * @return 学生实体 {@link StudentDTO}
     */
    @GetMapping("/school/{id}")
    public SchoolStudentVO getSchoolStudent(@PathVariable("id") Integer id) {
        return studentFacade.getStudentByStudentIdAndSchoolId(id);
    }

    /**
     * 更新学校学生信息
     * @param schoolStudent
     */
    @PostMapping("/school/save")
    public SchoolStudent saveSchoolStudent(@RequestBody SchoolStudentDTO schoolStudent) {
        return studentBizService.saveSchoolStudent(schoolStudent);
    }
}