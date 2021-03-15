package com.wupol.myopia.business.parent.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.service.SchoolGradeService;
import com.wupol.myopia.business.management.service.SchoolService;
import com.wupol.myopia.business.parent.domain.dto.CheckIdCardRequest;
import com.wupol.myopia.business.parent.domain.dto.CountReportItems;
import com.wupol.myopia.business.parent.domain.dto.VisitsReportDetailRequest;
import com.wupol.myopia.business.parent.service.ParentStudentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * 家长-孩子
 *
 * @author HaoHao
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/parent/parentStudent")
public class ParentStudentController {

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
        return parentStudentService.getStudentById(id);
    }

    @GetMapping("school/grade/list/{schoolId}")
    public Object schoolGradeList(@PathVariable("schoolId") Integer schoolId) {
        return schoolGradeService.getAllGradeList(schoolId);
    }

    @PutMapping("")
    public Object updateParentStudent(@RequestBody Student student) throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return parentStudentService.updateStudent(currentUser,student);
    }

    @PostMapping
    public Object saveParentStudent(@RequestBody Student student) throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return parentStudentService.saveStudent(student, currentUser);
    }

    @GetMapping("school/getSchools/{schoolName}")
    public Object getSchools(@PathVariable("schoolName") String schoolName) {
        return schoolService.getBySchoolName(schoolName);
    }

    @GetMapping("report/count/{id}")
    public Object studentReportCount(@PathVariable("id") Integer id) {
        return parentStudentService.studentReportCount(id);
    }

    @GetMapping("report/screening/list/{id}")
    public List<CountReportItems> getStudentCountReportItems(@PathVariable("id") Integer id) {
        return parentStudentService.getStudentCountReportItems(id);
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
    public Object visitsLatestReport(@PathVariable("id") Integer id) {
        return parentStudentService.latestVisitsReport(id);
    }

    @GetMapping("report/visits/detail")
    public Object getVisitsReportDetail(VisitsReportDetailRequest request) {
        return parentStudentService.getVisitsReportDetails(request);
    }

    @GetMapping("report/screening/visionTrends/{studentId}")
    public Object screeningVisionTrends(@PathVariable("studentId") Integer studentId) {
        return parentStudentService.screeningVisionTrends(studentId);
    }

    @GetMapping("/getQrCode/{studentId}")
    public Object getQrCode(@PathVariable("studentId")Integer studentId) {
        return ApiResult.success(parentStudentService.getQrCode(studentId));
    }
}
