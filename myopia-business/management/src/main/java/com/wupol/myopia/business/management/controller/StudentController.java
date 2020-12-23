package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.dto.StudentListRequest;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.domain.query.StudentQuery;
import com.wupol.myopia.business.management.facade.ExcelFacade;
import com.wupol.myopia.business.management.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
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
        student.setCreateUserId(Const.CREATE_USER_ID);
        return studentService.saveStudent(student);
    }

    @PutMapping()
    public Object updateStudent(@RequestBody Student student) {
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
    public Object getStudentsList(@Valid StudentListRequest request) throws ParseException {
        return studentService.getStudentLists(request);
    }

    @GetMapping("/export")
    public ApiResult getHospitalExportData(StudentQuery query) throws IOException {
        //TODO 待检验日期范围
        return ApiResult.success(excelFacade.generateStudent(query));
    }

    @PostMapping("/import")
    public ApiResult importStudent(MultipartFile file) throws IOException {
        Long schoolId = 12L;
        excelFacade.importStudent(schoolId, file);
        return ApiResult.success();
    }
}
