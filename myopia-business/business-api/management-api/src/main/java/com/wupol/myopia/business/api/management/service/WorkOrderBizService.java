package com.wupol.myopia.business.api.management.service;

import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.framework.api.service.VistelToolsService;
import com.wupol.framework.sms.domain.dto.MsgData;
import com.wupol.framework.sms.domain.dto.SmsResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.RegularUtils;
import com.wupol.myopia.business.aggregation.student.service.StudentFacade;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.WorkOrderStatusEnum;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.parent.domain.dos.StudentDO;
import com.wupol.myopia.business.core.parent.domain.dto.WorkOrderDTO;
import com.wupol.myopia.business.core.parent.domain.dto.WorkOrderQueryDTO;
import com.wupol.myopia.business.core.parent.domain.dto.WorkOrderRequestDTO;
import com.wupol.myopia.business.core.parent.domain.model.WorkOrder;
import com.wupol.myopia.business.core.parent.service.WorkOrderService;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 工单
 *
 * @Author xjl
 * @Date 2022/3/8
 */
@Service
@Log4j2
public class WorkOrderBizService {

    @Autowired
    private WorkOrderService workOrderService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Resource
    private VistelToolsService vistelToolsService;
    @Autowired
    private StudentFacade studentFacade;
    @Autowired
    private StatConclusionService statConclusionService;


    /**
     * 获取工单列表
     *
     * @param pageRequest       分页参数
     * @param workOrderQueryDTO 查询参数
     * @return 工单分页
     */
    public IPage<WorkOrderDTO> getWorkOrderPage(PageRequest pageRequest, WorkOrderQueryDTO workOrderQueryDTO) {

        // 模糊查询学校id组装
        if (StringUtils.isNotBlank(workOrderQueryDTO.getSchoolName())) {
            List<School> schoolList = schoolService.getBySchoolName(workOrderQueryDTO.getSchoolName());
            if (!CollectionUtils.isEmpty(schoolList)) {
                List<Integer> schoolIds = schoolList.stream().map(School::getId).collect(Collectors.toList());
                workOrderQueryDTO.setSchoolIds(schoolIds);
            }
        }
        if (Objects.nonNull(workOrderQueryDTO.getEndTime())) {
            workOrderQueryDTO.setEndTime(DateUtils.addDays(workOrderQueryDTO.getEndTime(), 1));
        }

        // 分页结果
        IPage<WorkOrderDTO> workOrderDTOIPage = workOrderService.getWorkOrderPage(pageRequest, workOrderQueryDTO);

        // 组装年级班级信息
        List<WorkOrderDTO> records = workOrderDTOIPage.getRecords();
        if (!CollectionUtils.isEmpty(records)) {

            List<Integer> schoolIds = records.stream().map(WorkOrder::getSchoolId).collect(Collectors.toList());
            List<School> schoolList = schoolService.getByIds(schoolIds);
            List<Integer> gradeIds = records.stream().map(WorkOrder::getGradeId).collect(Collectors.toList());
            List<SchoolGrade> schoolGradeList = schoolGradeService.getByIds(gradeIds);
            List<Integer> classIds = records.stream().map(WorkOrder::getClassId).collect(Collectors.toList());
            List<SchoolClass> schoolClassList = schoolClassService.getByIds(classIds);

            Map<Integer, String> schoolMap = null;
            Map<Integer, String> gradeMap = null;
            Map<Integer, String> classMap = null;
            if (!CollectionUtils.isEmpty(schoolList)) {
                schoolMap = schoolList.stream().collect(Collectors.toMap(School::getId, School::getName));
            }
            if (!CollectionUtils.isEmpty(schoolGradeList)) {
                gradeMap = schoolGradeList.stream().collect(Collectors.toMap(SchoolGrade::getId, SchoolGrade::getName));
            }
            if (!CollectionUtils.isEmpty(schoolClassList)) {
                classMap = schoolClassList.stream().collect(Collectors.toMap(SchoolClass::getId, SchoolClass::getName));
            }
            for (WorkOrderDTO workOrderDTO : records) {
                workOrderDTO.setSchoolName(Objects.isNull(schoolMap) ? null : schoolMap.get(workOrderDTO.getSchoolId()));
                workOrderDTO.setGradeName(Objects.isNull(gradeMap) ? null : gradeMap.get(workOrderDTO.getGradeId()));
                workOrderDTO.setClassName(Objects.isNull(classMap) ? null : classMap.get(workOrderDTO.getClassId()));
            }
        }
        return workOrderDTOIPage;
    }

    /**
     * 处理工单
     *
     * @param workOrderRequestDTO 工单处理请求参数
     */
    @Transactional(rollbackFor = Exception.class)
    public Student disposeOfWordOrder(WorkOrderRequestDTO workOrderRequestDTO) {
        School school = schoolService.getBySchoolId(workOrderRequestDTO.getSchoolId());

        if (Objects.isNull(school)) {
            throw new BusinessException("该学校不存在");
        }
        // 多端学生原始信息
        StudentDTO studentDTO = studentService.getStudentById(workOrderRequestDTO.getStudentId());

        Student student = null;
        // 工单身份证是否存在
        if (StringUtils.isNotBlank(workOrderRequestDTO.getIdCard())) {
            // 无修改身份证/护照直接更新多端学生信息
            if (StringUtils.equals(workOrderRequestDTO.getIdCard(), studentDTO.getIdCard())) {
                packageManagementStudent(studentDTO, workOrderRequestDTO);
                studentService.updateStudent(studentDTO);
                return studentDTO;
            }
            student = studentService.getAllByIdCard(workOrderRequestDTO.getIdCard());
        }
        if (StringUtils.isNotBlank(workOrderRequestDTO.getPassport())) {
            if (StringUtils.equals(workOrderRequestDTO.getPassport(), studentDTO.getPassport())) {
                packageManagementStudent(studentDTO, workOrderRequestDTO);
                studentService.updateStudent(studentDTO);
                return studentDTO;
            }
            student = studentService.getAllByPassport(workOrderRequestDTO.getPassport());
        }

        // 不存在工单身份证
        if (Objects.isNull(student)) {
            //  新增学生
            student = saveManagementStudent(studentDTO, workOrderRequestDTO);
        }
        // 更新身份证学生的基础信息
        packageManagementStudent(student, workOrderRequestDTO);
        student.setStatus(CommonConst.STATUS_NOT_DELETED);

        studentService.updateStudent(student);

        // 待修改筛查记录
        if (Objects.isNull(workOrderRequestDTO.getScreeningId())) {
            return student;
        }
        VisionScreeningResult visionScreeningResult = visionScreeningResultService.getById(workOrderRequestDTO.getScreeningId());
        if (Objects.isNull(visionScreeningResult)) {
            throw new BusinessException("筛查记录不存在");
        }
        // 筛查记录表学生id 筛查学生表的学生id更改
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.getById(visionScreeningResult.getScreeningPlanSchoolStudentId());
        if (Objects.isNull(screeningPlanSchoolStudent)) {
            throw new BusinessException("筛查学生不存在");
        }
        StatConclusion statConclusion = statConclusionService.getByResultId(visionScreeningResult.getId());

        if (Objects.isNull(statConclusion)){
            throw new BusinessException("筛查结果结论不存在");
        }
        statConclusionService.updateById(statConclusion.setStudentId(student.getId()));
        screeningPlanSchoolStudentService.updateById(screeningPlanSchoolStudent.setStudentId(student.getId()));
        visionScreeningResultService.updateById(visionScreeningResult.setStudentId(student.getId()));

        return student;

    }


    /**
     * 保存多端学生信息
     *
     * @param studentDTO          学生信息
     * @param workOrderRequestDTO 工单请求参数
     * @return 学生信息
     */
    private Student saveManagementStudent(StudentDTO studentDTO, WorkOrderRequestDTO workOrderRequestDTO) {
        Student saveStudent = new Student();
        BeanUtils.copyProperties(studentDTO, saveStudent);
        packageManagementStudent(saveStudent, workOrderRequestDTO);
        // 验证身份证
        if (StringUtils.isNotBlank(workOrderRequestDTO.getIdCard()) && !RegularUtils.isIdCard(workOrderRequestDTO.getIdCard())) {
            throw new BusinessException("证件号填写错误，请重新填写！");
        }
        // 验证护照
        if (StringUtils.isNotBlank(workOrderRequestDTO.getPassport()) && workOrderRequestDTO.getPassport().length() < 8) {
            throw new BusinessException("护照填写错误，请重新填写！");
        }

        CurrentUser user = CurrentUserUtil.getCurrentUser();
        saveStudent.setCreateUserId(user.getId());
        saveStudent.setId(studentFacade.saveStudentAndSchoolStudent(saveStudent));
        return saveStudent;
    }


    /**
     * 多端学生打包
     *
     * @param student             学生信息
     * @param workOrderRequestDTO 工单请求
     */
    private void packageManagementStudent(Student student, WorkOrderRequestDTO workOrderRequestDTO) {
        Assert.isTrue(!StringUtils.isAllBlank(workOrderRequestDTO.getIdCard(), workOrderRequestDTO.getPassport()), "身份证和护照不能全部为空");
        if (StringUtils.isNotEmpty(workOrderRequestDTO.getIdCard())) {
            student.setIdCard(workOrderRequestDTO.getIdCard());
            student.setPassport(null);
        } else {
            student.setIdCard(null);
            student.setPassport(workOrderRequestDTO.getPassport());
        }
        student.setSno(StringUtils.isBlank(workOrderRequestDTO.getSno()) ? "" : workOrderRequestDTO.getSno());
        student.setName(workOrderRequestDTO.getName());
        student.setGender(workOrderRequestDTO.getGender());
        student.setBirthday(workOrderRequestDTO.getBirthday());
        student.setSchoolId(workOrderRequestDTO.getSchoolId());
        student.setGradeId(workOrderRequestDTO.getGradeId());
        student.setClassId(workOrderRequestDTO.getClassId());
    }


    /**
     * 更新工单信息
     *
     * @param workOrderRequestDTO 工单请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateWorkOrderAndSendSMS(Student student,StudentDO studentDO, WorkOrderRequestDTO workOrderRequestDTO) {

        WorkOrder workOrder = workOrderService.getById(workOrderRequestDTO.getWorkOrderId());
        if (Objects.isNull(workOrder)) {
            throw new BusinessException("工单不存在");
        }
        // 发送短信
        MsgData msgData = new MsgData(workOrder.getParentPhone(), "+86", CommonConst.SEND_SMS_WORD_ORDER_DISPOSE_NOTICE);
        SmsResult smsResult = vistelToolsService.sendMsg(msgData);
        // 检查
        checkSendMsgStatus(smsResult, msgData, workOrder);

        studentDO.setScreeningCode(workOrderRequestDTO.getScreeningCode())
                .setScreeningDate(workOrderRequestDTO.getScreeningDate())
                .setScreeningTitle(workOrderRequestDTO.getScreeningTitle());

        StudentDO newData = getNewData(student);
        workOrder.setStatus(WorkOrderStatusEnum.PROCESSED.code)
                .setNewData(newData)
                .setContent(workOrderRequestDTO.getContent())
                .setOldData(studentDO);

        workOrderService.updateById(workOrder);
    }

    /**
     * 打包新学生数据
     * @param student
     * @return
     */
    private StudentDO getNewData(Student student) {
        StudentDO studentDO = new StudentDO();
        if (Objects.nonNull(student.getSchoolId())) {
            School school = schoolService.getBySchoolId(student.getSchoolId());
            if (Objects.nonNull(school)){
                studentDO.setSchoolName(school.getName());
            }
        }
        if (Objects.nonNull(student.getGradeId())){
            SchoolGrade schoolGrade = schoolGradeService.getById(student.getGradeId());
            if (Objects.nonNull(schoolGrade)){
                studentDO.setGradeName(schoolGrade.getName());
            }
        }
        if (Objects.nonNull(student.getClassId())){
            SchoolClass schoolClass = schoolClassService.getById(student.getClassId());
            if (Objects.nonNull(schoolClass)){
                studentDO.setClassName(schoolClass.getName());
            }
        }
        studentDO.setAddress(student.getAddress())
                .setBirthday(DateFormatUtil.format(student.getBirthday(), DateFormatUtil.FORMAT_ONLY_DATE))
                .setClassId(student.getClassId())
                .setGender(student.getGender())
                .setGradeId(student.getGradeId())
                .setGradeType(student.getGradeType())
                .setId(student.getId())
                .setIdCard(student.getIdCard())
                .setMpParentPhone(student.getMpParentPhone())
                .setName(student.getName())
                .setNation(student.getNation())
                .setParentPhone(student.getParentPhone())
                .setPassport(student.getPassport())
                .setSchoolId(student.getSchoolId())
                .setSno(StringUtils.isBlank(student.getSno())?null:student.getSno());
        return studentDO;
    }

    /**
     * 检查是否发送成功
     *
     * @param smsResult sms结果
     * @param msgData   信息
     * @param workOrder 工单
     */
    private void checkSendMsgStatus(SmsResult smsResult, MsgData msgData, WorkOrder workOrder) {
        if (smsResult.isSuccessful()) {
            workOrder.setIsNotice(true);
        } else {
            log.error("发送通知到手机号码错误，提交信息:{}, 异常信息:{}", JSONObject.toJSONString(msgData), smsResult);
        }
    }

    /**
     * 获取旧数据
     *
     * @param studentId 学生id
     * @return 学生信息
     */
    public StudentDO getOldData(Integer studentId) {
        StudentDTO student = studentService.getStudentById(studentId);
        StudentDO studentDO = new StudentDO();
        studentDO.setAddress(student.getAddress())
                .setBirthday(DateFormatUtil.format(student.getBirthday(), DateFormatUtil.FORMAT_ONLY_DATE))
                .setClassId(student.getClassId())
                .setClassName(student.getClassName())
                .setGender(student.getGender())
                .setGradeId(student.getGradeId())
                .setGradeName(student.getGradeName())
                .setGradeType(student.getGradeType())
                .setId(student.getId())
                .setIdCard(student.getIdCard())
                .setMpParentPhone(student.getMpParentPhone())
                .setName(student.getName())
                .setNation(student.getNation())
                .setParentPhone(student.getParentPhone())
                .setPassport(student.getPassport())
                .setSchoolId(student.getSchoolId())
                .setSchoolName(student.getSchoolName())
                .setSno(StringUtils.isBlank(student.getSno())?null:student.getSno());
        return studentDO;
    }

    /**
     * 处理工单
     * @param workOrderRequestDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void dispose(WorkOrderRequestDTO workOrderRequestDTO) {
        // 旧数据保存
        StudentDO studentDO = getOldData(workOrderRequestDTO.getStudentId());
        Student student = disposeOfWordOrder(workOrderRequestDTO);
        // 更新工单状态发送短信
        updateWorkOrderAndSendSMS(student,studentDO,workOrderRequestDTO);

    }
}
