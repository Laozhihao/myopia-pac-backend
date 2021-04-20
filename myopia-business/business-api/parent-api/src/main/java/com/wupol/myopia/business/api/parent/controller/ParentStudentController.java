package com.wupol.myopia.business.api.parent.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.hospital.domain.dto.StudentVisitReportResponseDTO;
import com.wupol.myopia.business.management.domain.dto.SchoolGradeItems;
import com.wupol.myopia.business.management.domain.dto.StudentDTO;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.service.DistrictService;
import com.wupol.myopia.business.management.service.SchoolGradeService;
import com.wupol.myopia.business.management.service.SchoolService;
import com.wupol.myopia.business.parent.service.ParentStudentService;

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

    @Resource
    private DistrictService districtService;

    /**
     * 获取孩子统计、孩子列表
     *
     * @param parentId 家长ID
     * @return 孩子统计、孩子列表
     */
    @GetMapping("count/{parentId}")
    public CountParentStudentResponseDTO countParentStudent(@PathVariable("parentId") Integer parentId) {
        return parentStudentService.countParentStudent(parentId);
    }

    /**
     * 检查身份证
     *
     * @param request 请求入参
     * @return 学生实体
     */
    @PostMapping("checkIdCard")
    public StudentDTO checkIdCard(@RequestBody CheckIdCardRequest request) {
        return parentStudentService.checkIdCard(request);
    }

    /**
     * 获取学生信息
     *
     * @param id 学生ID
     * @return 学生信息
     */
    @GetMapping("{id}")
    public StudentDTO getStudent(@PathVariable("id") Integer id) {
        return parentStudentService.getStudentById(id);
    }

    /**
     * 年级列表(没有分页)
     *
     * @param schoolId 学校ID
     * @return 年级列表
     */
    @GetMapping("school/grade/list/{schoolId}")
    public List<SchoolGradeItems> schoolGradeList(@PathVariable("schoolId") Integer schoolId) {
        return schoolGradeService.getAllGradeList(schoolId);
    }

    /**
     * 更新学生信息
     *
     * @param student 学生实体
     * @return 学生信息
     * @throws IOException IO异常
     */
    @PutMapping("")
    public StudentDTO updateParentStudent(@RequestBody Student student) throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return parentStudentService.updateStudent(currentUser, student);
    }

    /**
     * 更新孩子（没有绑定则绑定）
     *
     * @param student 学生信息
     * @return 更新个数
     * @throws IOException IO异常
     */
    @PostMapping
    public Integer saveParentStudent(@RequestBody Student student) throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return parentStudentService.saveStudent(student, currentUser);
    }

    /**
     * 通过名字获取学校
     *
     * @param schoolName 学校名称
     * @return 学校列表
     */
    @GetMapping("school/getSchools/{schoolName}")
    public List<School> getSchools(@PathVariable("schoolName") String schoolName) {
        return schoolService.getBySchoolName(schoolName);
    }

    /**
     * 学生报告统计
     *
     * @param id 学生ID
     * @return 学生报告统计
     */
    @GetMapping("report/count/{id}")
    public ReportCountResponseDTO studentReportCount(@PathVariable("id") Integer id) {
        return parentStudentService.studentReportCount(id);
    }

    /**
     * 学生筛查报告列表
     *
     * @param id 学生ID
     * @return 筛查报告列表
     */
    @GetMapping("report/screening/list/{id}")
    public List<CountReportItems> getStudentCountReportItems(@PathVariable("id") Integer id) {
        return parentStudentService.getStudentCountReportItems(id);
    }

    /**
     * 获取学生最新一次筛查结果
     *
     * @param id 学生ID
     * @return 筛查结果
     */
    @GetMapping("report/screening/latest/{id}")
    public ScreeningReportResponseDTO latestScreeningReport(@PathVariable("id") Integer id) {
        return parentStudentService.latestScreeningReport(id);
    }

    /**
     * 获取筛查结果详情
     *
     * @param id 学生ID
     * @return 筛查结果详情
     */
    @GetMapping("report/screening/detail/{id}")
    public ScreeningReportResponseDTO reportScreeningDetail(@PathVariable("id") Integer id) {
        return parentStudentService.getScreeningReportDetail(id);
    }

    /**
     * 最新的就诊报告
     *
     * @param id 学生ID
     * @return 就诊报告
     */
    @GetMapping("report/visits/latest/{id}")
    public StudentVisitReportResponseDTO visitsLatestReport(@PathVariable("id") Integer id) {
        return parentStudentService.latestVisitsReport(id);
    }

    /**
     * 获取就诊报告详情
     *
     * @param request 请求入参
     * @return 就诊报告详情
     */
    @GetMapping("report/visits/detail")
    public StudentVisitReportResponseDTO getVisitsReportDetail(VisitsReportDetailRequest request) {
        return parentStudentService.getVisitsReportDetails(request);
    }

    /**
     * 视力趋势
     *
     * @param studentId 学生ID
     * @return 视力趋势
     */
    @GetMapping("report/screening/visionTrends/{studentId}")
    public ScreeningVisionTrendsResponseDTO screeningVisionTrends(@PathVariable("studentId") Integer studentId) {
        return parentStudentService.screeningVisionTrends(studentId);
    }

    /**
     * 获取学生授权二维码
     *
     * @param studentId 学生ID
     * @return 学生授权二维码
     */
    @GetMapping("/getQrCode/{studentId}")
    public ApiResult<String> getQrCode(@PathVariable("studentId") Integer studentId) {
        return ApiResult.success(parentStudentService.getQrCode(studentId));
    }

    /**
     * 获取当前code的孩子节点
     *
     * @param areaCode 乡/镇code
     * @return List<District>
     */
    @GetMapping("/getTownInfo/{areaCode}")
    public List<District> getTownInfo(@PathVariable("areaCode") Long areaCode) {
        return districtService.getNextDistrictByCode(areaCode);
    }
}
