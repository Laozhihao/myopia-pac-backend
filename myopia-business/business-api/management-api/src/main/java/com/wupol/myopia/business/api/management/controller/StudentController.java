package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.excel.ExcelFacade;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExportExcelServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.aggregation.hospital.domain.dto.StudentVisitReportResponseDTO;
import com.wupol.myopia.business.aggregation.hospital.service.MedicalReportBizService;
import com.wupol.myopia.business.aggregation.student.service.StudentFacade;
import com.wupol.myopia.business.api.management.domain.vo.StudentWarningArchiveVO;
import com.wupol.myopia.business.api.management.service.StudentBizService;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.common.utils.constant.VisionLabels;
import com.wupol.myopia.business.common.utils.constant.VisionLabelsEnum;
import com.wupol.myopia.business.common.utils.domain.dto.Nation;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.hospital.domain.dos.ReportAndRecordDO;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.domain.dto.StudentQueryDTO;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentScreeningResultResponseDTO;
import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentCardResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.text.ParseException;
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
    private ExcelFacade excelFacade;

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentBizService studentBizService;

    @Autowired
    private ExportStrategy exportStrategy;

    @Autowired
    private MedicalReportBizService medicalReportBizService;

    @Autowired
    private StudentFacade studentFacade;

    /**
     * 新增学生
     *
     * @param student 学生实体
     * @return 新增数量
     */
    @PostMapping()
    public Integer saveStudent(@RequestBody @Valid Student student) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        student.setCreateUserId(user.getId());
        return studentService.saveStudent(student);
    }

    /**
     * 更新学生
     *
     * @param student 学生实体
     * @return 学生实体
     */
    @PutMapping()
    public StudentDTO updateStudent(@RequestBody @Valid Student student) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        student.setCreateUserId(user.getId());
        return studentBizService.updateStudentReturnCountInfo(student);
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
        return studentBizService.getStudentById(id);
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
     */
    @GetMapping("/export")
    public void getStudentExportData(Integer schoolId, Integer gradeId) throws IOException {
        Assert.isTrue(Objects.nonNull(schoolId), "学校id不能为空");
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        exportStrategy.doExport(new ExportCondition()
                        .setApplyExportFileUserId(user.getId())
                        .setSchoolId(schoolId)
                        .setGradeId(gradeId),
                ExportExcelServiceNameConstant.STUDENT_EXCEL_SERVICE);
    }

    /**
     * 导入学生列表
     *
     * @param file 导入文件
     * @throws ParseException 转换异常
     */
    @PostMapping("/import")
    public void importStudent(MultipartFile file, Integer schoolId) throws ParseException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        excelFacade.importStudent(currentUser.getId(), file, schoolId);
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
        return NationEnum.getNationList();
    }

    /**
     * 获取学生筛查档案
     *
     * @param id 学生ID
     * @return 学生筛查档案
     */
    @GetMapping("/screening/{id}")
    public StudentScreeningResultResponseDTO getScreeningList(@PathVariable("id") Integer id) {
        return studentFacade.getScreeningList(id);
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
     * @return List<MedicalReportDO>
     */
    @GetMapping("/report/list/{studentId}")
    public IPage<ReportAndRecordDO> getReportList(PageRequest pageRequest, @PathVariable("studentId") Integer studentId) {
        return studentBizService.getReportList(pageRequest, studentId);
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
    public List<StudentWarningArchiveVO> getStudentWarningArchive(@PathVariable("studentId") Integer studentId) {
        return studentBizService.getStudentWarningArchive(studentId);
    }

    @GetMapping("abc")
    public void abc(MultipartFile file) throws ParseException {
        excelFacade.importABVStudent(file);
    }
}