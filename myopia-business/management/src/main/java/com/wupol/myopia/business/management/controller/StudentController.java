package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.constant.NationEnum;
import com.wupol.myopia.business.management.constant.VisionLabelsEnum;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.ScreeningOrganizationStaffQuery;
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
    public Object saveStudent(@RequestBody @Valid Student student) {
        CurrentUser user = CurrentUserUtil.getLegalCurrentUser();
        student.setCreateUserId(user.getId());
        return studentService.saveStudent(student);
    }

    @PutMapping()
    public Object updateStudent(@RequestBody @Valid Student student) {
        CurrentUser user = CurrentUserUtil.getLegalCurrentUser();
        student.setCreateUserId(user.getId());
        return studentService.updateStudent(student);
    }

    @DeleteMapping("{id}")
    public Object deletedStudent(@PathVariable("id") Integer id) {
        CurrentUserUtil.getLegalCurrentUser();
        return studentService.deletedStudent(id);
    }

    @GetMapping("{id}")
    public Object getStudent(@PathVariable("id") Integer id) {
        CurrentUserUtil.getLegalCurrentUser();
        return studentService.getById(id);
    }

    @GetMapping("list")
    public Object getStudentsList(PageRequest pageRequest, StudentQuery studentQuery) throws ParseException {
        CurrentUserUtil.getLegalCurrentUser();
        return studentService.getStudentLists(pageRequest, studentQuery);
    }

    @GetMapping("/export")
    public ResponseEntity<FileSystemResource> getStudentExportData(StudentQuery query) throws IOException {
        //TODO 待检验日期范围
        return FileUtils.getResponseEntity(excelFacade.generateStudent(query));
    }

    @PostMapping("/import")
    public ApiResult importStudent(MultipartFile file) throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        excelFacade.importStudent(currentUser.getOrgId(), currentUser.getId(), file);
        return ApiResult.success();
    }

    @GetMapping("/import/demo")
    public ResponseEntity<FileSystemResource> getImportDemo() {
        return FileUtils.getResponseEntity(excelFacade.getStudentImportDemo());
    }

    @GetMapping("labels")
    public Object getVisionLabels() {
        CurrentUserUtil.getLegalCurrentUser();
        return VisionLabelsEnum.getVisionLabels();
    }

    @GetMapping("nation")
    public Object getNationLists() {
        CurrentUserUtil.getLegalCurrentUser();
        return NationEnum.getNationList();
    }
}