package com.wupol.myopia.business.management.controller;

import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.constant.NationEnum;
import com.wupol.myopia.business.management.constant.VisionLabelsEnum;
import com.wupol.myopia.business.management.domain.dto.StudentDTO;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.StudentQuery;
import com.wupol.myopia.business.management.facade.ExcelFacade;
import com.wupol.myopia.business.management.service.StudentService;
import com.wupol.myopia.business.management.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.xml.bind.ValidationException;
import java.io.IOException;
import java.text.ParseException;

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

    /**
     * 新增学生
     *
     * @param student 学生实体
     * @return 新增数量
     */
    @PostMapping()
    public Object saveStudent(@RequestBody @Valid Student student) {
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
    public Object updateStudent(@RequestBody @Valid Student student) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        student.setCreateUserId(user.getId());
        return studentService.updateStudent(student);
    }

    /**
     * 删除学生
     *
     * @param id 学生ID
     * @return 删除数量
     */
    @DeleteMapping("{id}")
    public Object deletedStudent(@PathVariable("id") Integer id) {
        return studentService.deletedStudent(id);
    }

    /**
     * 获取学生详情
     *
     * @param id 学生ID
     * @return 学生实体 {@link StudentDTO}
     */
    @GetMapping("{id}")
    public Object getStudent(@PathVariable("id") Integer id) {
        return studentService.getStudentById(id);
    }

    /**
     * 获取学生列表
     *
     * @param pageRequest  分页查询
     * @param studentQuery 请求条件
     * @return 学生列表
     */
    @GetMapping("list")
    public Object getStudentsList(PageRequest pageRequest, StudentQuery studentQuery) {
        return studentService.getStudentLists(pageRequest, studentQuery);
    }

    /**
     * 导出学生列表
     *
     * @param schoolId 学校ID
     * @param gradeId  年级ID
     * @return 是否成功
     * @throws IOException         IO异常
     * @throws ValidationException 检验异常
     * @throws UtilException       工具异常
     */
    @GetMapping("/export")
    public Object getStudentExportData(Integer schoolId, Integer gradeId) throws IOException, ValidationException, UtilException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        excelFacade.generateStudent(user.getId(), schoolId, gradeId);
        return ApiResult.success();
    }

    /**
     * 导入学生列表
     *
     * @param file 导入文件
     * @return 是否成功
     * @throws ParseException 转换异常
     */
    @PostMapping("/import")
    public ApiResult importStudent(MultipartFile file) throws ParseException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        excelFacade.importStudent(currentUser.getId(), file);
        return ApiResult.success();
    }

    /**
     * 导入模板
     *
     * @return File
     * @throws IOException IO异常
     */
    @GetMapping("/import/demo")
    public ResponseEntity<FileSystemResource> getImportDemo() throws IOException {
        return FileUtils.getResponseEntity(excelFacade.getStudentImportDemo());
    }

    /**
     * 获取视力标签
     *
     * @return 视力标签
     */
    @GetMapping("labels")
    public Object getVisionLabels() {
        return VisionLabelsEnum.getVisionLabels();
    }

    /**
     * 获取民族列表
     *
     * @return 民族列表
     */
    @GetMapping("nation")
    public Object getNationLists() {
        return NationEnum.getNationList();
    }

    /**
     * 获取学生筛查档案
     *
     * @param id 学生ID
     * @return 学生筛查档案
     */
    @GetMapping("/screening/{id}")
    public Object getScreeningList(@PathVariable("id") Integer id) {
        return studentService.getScreeningList(id);
    }

    /**
     * 获取学生档案卡
     *
     * @param resultId 筛查结果ID
     * @return 学生档案卡
     */
    @GetMapping("/screening/card/{resultId}")
    public Object getCardDetails(@PathVariable("resultId") Integer resultId) {
        return studentService.packageCardDetails(resultId);
    }
}