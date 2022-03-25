package com.wupol.myopia.business.api.parent.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.base.util.RegularUtils;
import com.wupol.myopia.business.aggregation.hospital.domain.dto.StudentVisitReportResponseDTO;
import com.wupol.myopia.business.api.parent.domain.dos.CountReportItemsDO;
import com.wupol.myopia.business.api.parent.domain.dos.ReportCountResponseDO;
import com.wupol.myopia.business.api.parent.domain.dto.*;
import com.wupol.myopia.business.api.parent.service.ParentStudentBizService;
import com.wupol.myopia.business.core.common.domain.dto.SuggestHospitalDTO;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.hospital.domain.dto.EyeHealthyReportResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.PreschoolCheckRecordDTO;
import com.wupol.myopia.business.core.hospital.service.PreschoolCheckRecordService;
import com.wupol.myopia.business.core.parent.domain.dto.CheckIdCardRequestDTO;
import com.wupol.myopia.business.core.parent.domain.model.Parent;
import com.wupol.myopia.business.core.parent.domain.model.WorkOrder;
import com.wupol.myopia.business.core.parent.service.ParentService;
import com.wupol.myopia.business.core.parent.service.WorkOrderService;
import com.wupol.myopia.business.core.school.domain.dto.CountParentStudentResponseDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeItemsDTO;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
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
    private DistrictService districtService;
    @Resource
    private ParentStudentBizService parentStudentBizService;
    @Resource
    private PreschoolCheckRecordService preschoolCheckRecordService;
    @Resource
    private WorkOrderService workOrderService;
    @Resource
    private ParentService parentService;
    /**
     * 获取孩子统计、孩子列表
     *
     * @return 孩子统计、孩子列表
     */
    @GetMapping("count")
    public CountParentStudentResponseDTO countParentStudent() {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return parentStudentBizService.countParentStudent(currentUser);
    }

    /**
     * 检查身份证
     *
     * @param request 请求入参
     * @return 学生实体
     */
    @PostMapping("checkIdCard")
    public StudentDTO checkIdCard(@RequestBody CheckIdCardRequestDTO request) {
        return parentStudentBizService.checkIdCard(request);
    }

    /**
     * 获取学生信息
     *
     * @param id 学生ID
     * @return 学生信息
     */
    @GetMapping("{id}")
    public StudentDTO getStudent(@PathVariable("id") Integer id) {
        return parentStudentBizService.getStudentById(id);
    }

    /**
     * 年级列表(没有分页)
     *
     * @param schoolId 学校ID
     * @return 年级列表
     */
    @GetMapping("school/grade/list/{schoolId}")
    public List<SchoolGradeItemsDTO> schoolGradeList(@PathVariable("schoolId") Integer schoolId) {
        return schoolGradeService.getAllGradeList(schoolId);
    }

    /**
     * 更新学生信息
     *
     * @param student 学生实体
     * @return 学生信息
     */
    @PutMapping("")
    public StudentDTO updateParentStudent(@RequestBody Student student) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return parentStudentBizService.updateStudent(currentUser, student);
    }

    /**
     * 新增孩子（没有绑定则绑定）
     *
     * @param student 学生信息
     * @return 更新个数
     */
    @PostMapping
    public Integer saveParentStudent(@RequestBody Student student) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return parentStudentBizService.saveStudent(student, currentUser);
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
    public ReportCountResponseDO studentReportCount(@PathVariable("id") Integer id) {
        return parentStudentBizService.studentReportCount(id);
    }

    /**
     * 学生筛查报告列表
     *
     * @param id 学生ID
     * @return 筛查报告列表
     */
    @GetMapping("report/screening/list/{id}")
    public List<CountReportItemsDO> getStudentCountReportItems(@PathVariable("id") Integer id) {
        return parentStudentBizService.getStudentCountReportItems(id);
    }

    /**
     * 获取学生最新一次筛查结果
     *
     * @param id 学生ID
     * @return 筛查结果
     */
    @GetMapping("report/screening/latest/{id}")
    public ScreeningReportResponseDTO latestScreeningReport(@PathVariable("id") Integer id) {
        return parentStudentBizService.latestScreeningReport(id);
    }

    /**
     * 获取筛查结果详情
     *
     * @param id         学生ID
     * @param isShowBind 是否展示重新绑定身份证
     * @return 筛查结果详情
     */
    @GetMapping("report/screening/detail/{id}")
    public ScreeningReportResponseDTO reportScreeningDetail(@PathVariable("id") Integer id, @NotNull(message = "辨识位不能为空") boolean isShowBind) {
        return parentStudentBizService.getScreeningReportDetail(id, isShowBind);
    }

    /**
     * 最新的就诊报告
     *
     * @param id 学生ID
     * @return 就诊报告
     */
    @GetMapping("report/visits/latest/{id}")
    public StudentVisitReportResponseDTO visitsLatestReport(@PathVariable("id") Integer id) {
        return parentStudentBizService.latestVisitsReport(id);
    }

    /**
     * 获取就诊报告详情
     *
     * @param request 请求入参
     * @return 就诊报告详情
     */
    @GetMapping("report/visits/detail")
    public StudentVisitReportResponseDTO getVisitsReportDetail(VisitsReportDetailRequest request) {
        return parentStudentBizService.getVisitsReportDetails(request);
    }

    /**
     * 视力趋势
     *
     * @param studentId 学生ID
     * @return 视力趋势
     */
    @GetMapping("report/screening/visionTrends/{studentId}")
    public ScreeningVisionTrendsResponseDTO screeningVisionTrends(@PathVariable("studentId") Integer studentId) {
        return parentStudentBizService.screeningVisionTrends(studentId);
    }

    /**
     * 获取学生授权二维码
     *
     * @param studentId 学生ID
     * @return 学生授权二维码
     */
    @GetMapping("/getQrCode/{studentId}")
    public ApiResult<String> getQrCode(@PathVariable("studentId") Integer studentId) {
        return ApiResult.success(parentStudentBizService.getQrCode(studentId));
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

    /**
     * 获取推荐医院列表
     *
     * @param screeningOrgId 筛查机构Id
     * @return 推荐医院列表
     */
    @GetMapping("/getCooperationHospital/{screeningOrgId}")
    public List<SuggestHospitalDTO> getCooperationHospital(@PathVariable("screeningOrgId") Integer screeningOrgId) {
        return parentStudentBizService.getCooperationHospital(screeningOrgId);
    }

    /**
     * 获取学生眼保健检查报告
     *
     * @param studentId 学生Id
     * @return 眼保健检查报告列表
     */
    @GetMapping("eyeHealthyReport/list/{studentId}")
    public List<EyeHealthyReportResponseDTO> getEyeHealthyReportList(@PathVariable("studentId") Integer studentId) {
        return preschoolCheckRecordService.getByStudentId(studentId);
    }

    /**
     * 获取学生最新一条眼保健检查报告
     *
     * @param studentId 学生Id
     * @return 眼保健检查报告列表
     */
    @GetMapping("eyeHealthyReport/latest/{studentId}")
    public PreschoolCheckRecordDTO getLatestEyeHealthyReportList(@PathVariable("studentId") Integer studentId) {
        return parentStudentBizService.getLatestEyeHealthyReportList(studentId);
    }

    /**
     * 获取学生眼保健检查详情
     *
     * @param id 报告Id
     * @return 详情
     */
    @GetMapping("eyeHealthyReport/{id}")
    public PreschoolReportDTO getEyeHealthyReportDetail(@PathVariable Integer id) {
        return parentStudentBizService.getEyeHealthyReportDetail(id);
    }

    /**
     * 通过条件获取筛查记录
     *
     * @param condition 条件
     * @param name      学生名称
     * @return 筛查条件
     */
    @GetMapping("report/screening/byCondition")
    public ScreeningReportInfoResponseDTO getScreeningReportByCondition(String condition, String name) {
        return parentStudentBizService.getScreeningReportByCondition(condition, name);
    }

    /**
     * 更新学生身份证
     *
     * @param requestDTO 请求入参
     * @param studentId  学生Id
     */
    @PostMapping("updateStudentIdCard/{studentId}")
    public void updateStudentIdCard(@RequestBody @Validated BindStudentRequestDTO requestDTO,
                                    @PathVariable("studentId") @NotNull(message = "学生Id不能为空") Integer studentId) {
        parentStudentBizService.updateStudentIdCard(requestDTO, studentId);
    }

    /**
     * 获取学生信息
     *
     * @param studentId 学生Id
     * @return StudentDTO
     */
    @GetMapping("getStudentInfo/{studentId}")
    public StudentDTO getStudentInfo(@PathVariable("studentId") Integer studentId) {
        return parentStudentBizService.getStudentInfo(studentId);
    }

    /**
     * 通过committeeCode获取区域
     *
     * @param committeeCode committeeCode
     * @return 区域
     */
    @GetMapping("getCommitteeList")
    public List<District> getCommitteeLists(@NotNull(message = "committeeCode不能为空") Long committeeCode) {
        return districtService.getDistrictPositionDetail(committeeCode);
    }

    /**
     * 新建工单
     * @param workOrder
     */
    @PostMapping("addWorkerOrder")
    public void addWorkOrder(@RequestBody @Validated WorkOrder workOrder){
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        workOrder.setCreateUserId(user.getId());
        if (StringUtils.isBlank(workOrder.getIdCard()) && StringUtils.isBlank(workOrder.getPassport())){
            throw new BusinessException("身份证或者护照信息不能为空！");
        }
        if (StringUtils.isNotBlank(workOrder.getIdCard()) && !RegularUtils.isIdCard(workOrder.getIdCard())) {
            throw new BusinessException("证件号填写错误，请重新填写！");
        }
        if (StringUtils.isNotBlank(workOrder.getPassport()) && !RegularUtils.isPassport(workOrder.getPassport())) {
            throw new BusinessException("护照填写错误，请重新填写！");
        }
        Parent parent = parentService.getParentByUserId(user.getId());
        workOrderService.addWorkOrder(workOrder,parent);
    }

    /**
     * 工单查看状态
     */
    @GetMapping("workOrderState")
    public int workOrderState(){
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return workOrderService.workOrderState(user.getId());
    }

    /**
     * 工单列表
     */
    @GetMapping("workOrderList")
    public List<WorkOrder> workOrderList(){
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return workOrderService.findByUserId(user.getId());
    }
}
