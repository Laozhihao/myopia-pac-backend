package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.RegularUtils;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.constant.VisionLabelsEnum;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.StudentQuery;
import com.wupol.myopia.business.management.facade.ExcelFacade;
import com.wupol.myopia.business.management.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;

/**
 * @Author HaoHao
 * @Date 2020-12-22
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


    @PostMapping()
    public Object saveStudent(@RequestBody Student student) {
        checkIsLegal(student);
        student.setCreateUserId(Const.CREATE_USER_ID);
        return studentService.saveStudent(student);
    }

    @PutMapping()
    public Object updateStudent(@RequestBody Student student) {
        checkIsLegal(student);
        student.setCreateUserId(Const.CREATE_USER_ID);
        return studentService.updateStudent(student);
    }

    @DeleteMapping("{id}")
    public Object deletedStudent(@PathVariable("id") Integer id) {
        return studentService.deletedStudent(id);
    }

    @GetMapping("{id}")
    public Object getStudent(@PathVariable("id") Integer id) {
        return studentService.getById(id);
    }

    @GetMapping("list")
    public Object getStudentsList(PageRequest pageRequest, StudentQuery studentQuery) throws ParseException {
        return studentService.getStudentLists(pageRequest, studentQuery);
    }

    @GetMapping("/export")
    public ApiResult getHospitalExportData(StudentQuery query) throws IOException {
        //TODO 待检验日期范围
        return ApiResult.success(excelFacade.generateStudent(query));
    }

    @PostMapping("/import")
    public ApiResult importStudent(MultipartFile file) throws IOException {
        Integer schoolId = 12;
        Integer createUserId = 12;
        excelFacade.importStudent(schoolId, createUserId, file);
        return ApiResult.success();
    }

    @GetMapping("labels")
    public Object getVisionLabels() {
        return VisionLabelsEnum.getVisionLabels();
    }

    private void checkIsLegal(Student student) {

        // 检查身份证
        if (!RegularUtils.isIdCard(student.getIdCard())) {
            throw new BusinessException("身份证不正确");
        }
        // 检查手机号码
        if (!RegularUtils.isMobile(student.getParentPhone())) {
            throw new BusinessException("手机号不正确");

        }
    }
}
