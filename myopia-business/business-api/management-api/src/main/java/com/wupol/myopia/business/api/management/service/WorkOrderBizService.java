package com.wupol.myopia.business.api.management.service;

import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.framework.api.service.VistelToolsService;
import com.wupol.framework.sms.domain.dto.MsgData;
import com.wupol.framework.sms.domain.dto.SmsResult;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.WorkOrderStatusEnum;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.util.AgeUtil;
import com.wupol.myopia.business.common.utils.util.SerializationUtil;
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
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Resource
    private VistelToolsService vistelToolsService;


    /**
     * 获取工单列表
     *
     * @param pageRequest
     * @param workOrderQueryDTO
     * @return
     */
    public IPage<WorkOrderDTO> getWorkOrderList(PageRequest pageRequest, WorkOrderQueryDTO workOrderQueryDTO) {
        // 模糊查询学校id组装
        if (StringUtils.isNotBlank(workOrderQueryDTO.getSchoolName())) {
            List<School> schoolList = schoolService.getBySchoolName(workOrderQueryDTO.getSchoolName());
            if (!CollectionUtils.isEmpty(schoolList)) {
                List<Integer> schoolIds = schoolList.stream().map(School::getId).collect(Collectors.toList());
                workOrderQueryDTO.setSchoolIds(schoolIds);
            }
        }

        // 分页结果
        IPage<WorkOrderDTO> workOrderDTOIPage = workOrderService.getWorkOrderLists(pageRequest, workOrderQueryDTO);

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
     * @param workOrderRequestDTO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void disposeOfWordOrder(WorkOrderRequestDTO workOrderRequestDTO) {
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
                return;
            }
            student = studentService.getAllByIdCard(workOrderRequestDTO.getIdCard());
        }
        if (StringUtils.isNotBlank(workOrderRequestDTO.getPassport())) {
            if (StringUtils.equals(workOrderRequestDTO.getPassport(), studentDTO.getPassport())) {
                packageManagementStudent(studentDTO, workOrderRequestDTO);
                studentService.updateStudent(studentDTO);
                return;
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
        studentService.updateById(student);

        // 待修改筛查记录
        if (Objects.isNull(workOrderRequestDTO.getScreeningId())) {
            return;
        }
        VisionScreeningResult visionScreeningResult = visionScreeningResultService.getById(workOrderRequestDTO.getScreeningId());
        if (Objects.isNull(visionScreeningResult)) {
            throw new BusinessException("筛查记录不存在");
        }
        // 筛查记录表的筛查学校id层级学生id 筛查学生表的层级学校id
        updateStudentAndScreeningPlanSchoolStudentAndVisionScreeningResult(school, student, visionScreeningResult);


    }

    /**
     * 保存多端学生信息
     *
     * @param studentDTO
     * @param workOrderRequestDTO
     * @return
     */
    private Student saveManagementStudent(StudentDTO studentDTO, WorkOrderRequestDTO workOrderRequestDTO) {
        Student saveStudent = new Student();
        BeanUtils.copyProperties(studentDTO, saveStudent);
        packageManagementStudent(saveStudent, workOrderRequestDTO);
        saveStudent.setId(null);
        studentService.saveStudent(saveStudent);
        return saveStudent;
    }

    /**
     * 更新筛查记录，筛查学生
     *
     * @param school
     * @param student
     * @param visionScreeningResult
     */
    private void updateStudentAndScreeningPlanSchoolStudentAndVisionScreeningResult(School school, Student student, VisionScreeningResult visionScreeningResult) {
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.getById(visionScreeningResult.getScreeningPlanSchoolStudentId());
        if (Objects.isNull(screeningPlanSchoolStudent)) {
            throw new BusinessException("筛查学生不存在");
        }
        screeningPlanSchoolStudent.setIdCard(student.getIdCard())
                .setSchoolDistrictId(school.getDistrictId())
                .setSchoolId(school.getId())
                .setSchoolName(school.getName())
                .setStudentId(student.getId())
                .setPassport(student.getPassport())
                .setStudentName(student.getName())
                .setGradeId(student.getGradeId())
                .setClassId(student.getClassId())
                .setBirthday(student.getBirthday())
                .setGender(student.getGender())
                .setStudentAge(AgeUtil.countAge(student.getBirthday()))
                .setStudentSituation(SerializationUtil.serializeWithoutException(student))
                .setStudentNo(student.getSno());
        screeningPlanSchoolStudentService.updateById(screeningPlanSchoolStudent);

        visionScreeningResult.setSchoolId(school.getId())
                .setDistrictId(school.getDistrictId())
                .setStudentId(student.getId());
        visionScreeningResultService.updateById(visionScreeningResult);


    }


    /**
     * 多端学生打包
     *
     * @param student
     * @param workOrderRequestDTO
     */
    private void packageManagementStudent(Student student, WorkOrderRequestDTO workOrderRequestDTO) {
        if (StringUtils.isNotEmpty(workOrderRequestDTO.getPassport())) {
            student.setPassport(workOrderRequestDTO.getPassport());
        }
        if (StringUtils.isNotEmpty(workOrderRequestDTO.getIdCard())) {
            student.setIdCard(workOrderRequestDTO.getIdCard());
        }
        if (StringUtils.isNotEmpty(workOrderRequestDTO.getSno())) {
            student.setSno(workOrderRequestDTO.getSno());
        }
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
     * @param workOrderRequestDTO
     */
    public void updateWorkOrderAndSendSMS(StudentDO studentDO, WorkOrderRequestDTO workOrderRequestDTO) {

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


        workOrderService.updateById(workOrder.setContent(workOrderRequestDTO.getContent())
                .setStatus(WorkOrderStatusEnum.PROCESSED.code)
                .setScreeningId(workOrderRequestDTO.getScreeningId())
                .setOldData(studentDO));
    }

    /**
     * 检查是否发送成功
     *
     * @param smsResult
     * @param msgData
     * @param workOrder
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
     * @param studentId
     * @return
     */
    public StudentDO getOldData(Integer studentId) {
        StudentDTO student = studentService.getStudentById(studentId);
        StudentDO studentDO = new StudentDO();
        BeanUtils.copyProperties(student,studentDO);
        return studentDO;
    }
}
