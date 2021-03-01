package com.wupol.myopia.business.parent.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.service.SchoolGradeService;
import com.wupol.myopia.business.management.service.SchoolService;
import com.wupol.myopia.business.management.service.StudentService;
import com.wupol.myopia.business.parent.domain.dto.CheckIdCardRequest;
import com.wupol.myopia.business.parent.service.ParentStudentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Author HaoHao
 * @Date 2021-02-26
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/parent/parentStudent")
public class ParentStudentController {

    @Resource
    private StudentService studentService;

    @Resource
    private SchoolGradeService schoolGradeService;

    @Resource
    private SchoolService schoolService;

    @Resource
    private ParentStudentService parentStudentService;

    @GetMapping("count/{parentId}")
    public Object countParentStudent(@PathVariable("parentId") Integer parentId) {
        return parentStudentService.countParentStudent(parentId);
    }

    @PostMapping("checkIdCard")
    public Object checkIdCard(@RequestBody CheckIdCardRequest request) {
        return parentStudentService.checkIdCard(request);
    }

    @GetMapping("{id}")
    public Object getStudent(@PathVariable("id") Integer id) {
        return studentService.getById(id);
    }

    @GetMapping("school/grade/list/{schoolId}")
    public Object schoolGradeList(@PathVariable("schoolId") Integer schoolId) {
        return schoolGradeService.getAllGradeList(schoolId);
    }

    @PutMapping("")
    public Object updateParentStudent(@RequestBody Student student) {
        return studentService.updateStudent(student);
    }

    @PostMapping
    public Object saveParentStudent(@RequestBody Student student) {
        return studentService.saveStudent(student);
    }

    @GetMapping("school/getSchools/{schoolName}")
    public Object getSchools(@PathVariable("schoolName") String schoolName) {
        return schoolService.getBySchoolName(schoolName);
    }

    @GetMapping("report/count/{id}")
    public Object studentReportCount(@PathVariable("id") Integer id) {
        return parentStudentService.studentReportCount(id);
    }

    @GetMapping("report/screening/latest/{id}")
    public Object latestScreeningReport(@PathVariable("id") Integer id) {
        return parentStudentService.latestScreeningReport(id);
    }

    @GetMapping("report/screening/detail/{id}")
    public Object reportScreeningDetail(@PathVariable("id") Integer id) {
        return parentStudentService.getScreeningReportDetail(id);
    }

    @GetMapping("report/visits/latest/{id}")
    public Object visitslatestReport(@PathVariable("id") Integer id) {
        return parentStudentService.latestVisitsReport(id);
    }

    @GetMapping("report/visits/detail/{id}")
    public Object getVisitsReportDetail(@PathVariable("id") Integer id) {
        return parentStudentService.getVisitsReportDetails(id);
    }

    @GetMapping("report/screening/visionTrends/{studentId}")
    public Object screeningVisionTrends(@PathVariable("studentId") Integer studentId) {
        return parentStudentService.screeningVisionTrends(studentId);
    }
}
