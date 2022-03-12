package com.wupol.myopia.business.api.management.service;
import cn.hutool.core.lang.Assert;
import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.common.utils.constant.WorkOrderStatusEnum;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.util.AgeUtil;
import com.wupol.myopia.business.common.utils.util.SerializationUtil;
import com.wupol.myopia.business.core.parent.domain.dto.WorkOrderDTO;
import com.wupol.myopia.business.core.parent.domain.dto.WorkOrderQueryDTO;
import com.wupol.myopia.business.core.parent.domain.dto.WorkOrderRequestDTO;
import com.wupol.myopia.business.core.parent.domain.model.WorkOrder;
import com.wupol.myopia.business.core.parent.service.WorkOrderService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.ScreeningCodeGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 工单
 * @Author xjl
 * @Date 2022/3/8
 */
@Service
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


    /**
     * 获取工单列表
     * @param pageRequest
     * @param workOrderQueryDTO
     * @return
     */
    public IPage<WorkOrderDTO> getWorkOrderList(PageRequest pageRequest, WorkOrderQueryDTO workOrderQueryDTO) {
        // 模糊查询学校id组装
        if (!StringUtils.isEmpty(workOrderQueryDTO.getSchoolName())){
            List<School> schoolList = schoolService.getBySchoolName(workOrderQueryDTO.getName());
            if (!CollectionUtils.isEmpty(schoolList)) {
                List<Integer> schoolIds = schoolList.stream().map(School::getId).collect(Collectors.toList());
                workOrderQueryDTO.setSchoolIds(schoolIds);
            }
        }
        // 分页结果
        IPage<WorkOrderDTO> workOrderDTOIPage= workOrderService.getWorkOrderLists(pageRequest, workOrderQueryDTO);
        // 组装年级班级信息
        List<WorkOrderDTO> records = workOrderDTOIPage.getRecords();
        if (!CollectionUtils.isEmpty(records)){

            List<Integer> schoolIds = records.stream().map(WorkOrder::getSchoolId).collect(Collectors.toList());
            List<School> schoolList = schoolService.getByIds(schoolIds);
            List<Integer> gradeIds = records.stream().map(WorkOrder::getGradeId).collect(Collectors.toList());
            List<SchoolGrade> schoolGradeList = schoolGradeService.getByIds(gradeIds);
            List<Integer> classIds = records.stream().map(WorkOrder::getClassId).collect(Collectors.toList());
            List<SchoolClass> schoolClassList = schoolClassService.getByIds(classIds);

            Map<Integer, String> schoolMap = null;
            Map<Integer, String> gradeMap = null;
            Map<Integer, String> classMap = null;
            if (CollectionUtils.isEmpty(schoolList)) {
                schoolMap = schoolList.stream().collect(Collectors.toMap(School::getId, School::getName));
            }
            if (CollectionUtils.isEmpty(schoolGradeList)) {
                gradeMap = schoolGradeList.stream().collect(Collectors.toMap(SchoolGrade::getId, SchoolGrade::getName));
            }
            if (CollectionUtils.isEmpty(schoolClassList)) {
                classMap = schoolClassList.stream().collect(Collectors.toMap(SchoolClass::getId, SchoolClass::getName));
            }
            for (WorkOrderDTO record : records) {
                record.setSchoolName(Objects.isNull(schoolMap)?null:schoolMap.get(record.getSchoolId()));
                record.setGradeName(Objects.isNull(gradeMap)?null:gradeMap.get(record.getGradeId()));
                record.setSchoolName(Objects.isNull(classMap)?null:classMap.get(record.getClassId()));
            }
        }
        return workOrderDTOIPage;
    }

    /**
     * 处理工单
     * @param workOrderRequestDTO
     * @return
     */
    public void disposeOfWordOrder(WorkOrderRequestDTO workOrderRequestDTO) {
        School school = schoolService.getBySchoolId(workOrderRequestDTO.getSchoolId());
        if (Objects.isNull(school)){
            throw new BusinessException("学校不存在");
        }
        // 多端学生原始信息
        StudentDTO studentDTO = studentService.getStudentById(workOrderRequestDTO.getStudentId());

        packageManagementStudent(studentDTO,workOrderRequestDTO);

        // 无修改身份证/护照直接更新多端学生信息
        if (StringUtils.equals(workOrderRequestDTO.getIdCard(),studentDTO.getIdCard())||StringUtils.equals(workOrderRequestDTO.getPassport(),studentDTO.getPassport())){
            studentService.updateStudent(studentDTO);
        }

        // 待修改筛查记录
        if (Objects.isNull(workOrderRequestDTO.getScreeningId())){
            throw new BusinessException("筛查记录id为空");
        }
        VisionScreeningResult visionScreeningResult = visionScreeningResultService.getById(workOrderRequestDTO.getScreeningId());
        if (Objects.isNull(visionScreeningResult)){
            throw new BusinessException("筛查记录不存在");
        }

        // 工单身份证是否存在
        if (StringUtils.isNotEmpty(workOrderRequestDTO.getIdCard())) {
            Student student = studentService.getAllByIdCard(workOrderRequestDTO.getIdCard());
            // 不存在工单身份证
            if (Objects.isNull(student)){
                //  新增学生
                Student saveStudent = new Student();
                BeanUtils.copyProperties(studentDTO, saveStudent);
                saveStudent.setId(null);
                Integer saveStudentId = studentService.saveStudent(saveStudent);

                // 新增筛查学生 screening_plan_school_student
                ScreeningPlanSchoolStudent screeningPlanSchoolStudent = new ScreeningPlanSchoolStudent();
                ScreeningPlan screeningPlan = screeningPlanService.getById(visionScreeningResult.getPlanId());
                if (Objects.isNull(screeningPlan)){
                    throw new BusinessException("筛查结果中筛查计划不存在");
                }
                Long screeningCode = ScreeningCodeGenerator.nextId();
                screeningPlanSchoolStudent.setIdCard(workOrderRequestDTO.getIdCard())
                        .setSrcScreeningNoticeId(screeningPlan.getSrcScreeningNoticeId())
                        .setScreeningTaskId(screeningPlan.getScreeningTaskId())
                        .setScreeningPlanId(screeningPlan.getId())
                        .setPlanDistrictId(screeningPlan.getDistrictId())
                        .setSchoolDistrictId(school.getDistrictId())
                        .setSchoolId(workOrderRequestDTO.getSchoolId())
                        .setSchoolName(school.getName())
                        .setStudentId(saveStudent.getId())
                        .setPassport(workOrderRequestDTO.getPassport())
                        .setScreeningCode(screeningCode)
                        .setStudentName(saveStudent.getName())
                        .setGradeId(saveStudent.getGradeId())
                        .setClassId(saveStudent.getClassId())
                        .setBirthday(saveStudent.getBirthday())
                        .setGender(saveStudent.getGender())
                        .setStudentAge(AgeUtil.countAge(saveStudent.getBirthday()))
                        .setStudentSituation(SerializationUtil.serializeWithoutException(saveStudent))
                        .setStudentNo(saveStudent.getSno());
                screeningPlanSchoolStudentService.save(screeningPlanSchoolStudent);

                // 修改筛查记录
                visionScreeningResult.setStudentId(saveStudentId);
                visionScreeningResult.setDistrictId(school.getDistrictId());
                visionScreeningResult.setSchoolId(workOrderRequestDTO.getSchoolId());
                visionScreeningResult.setScreeningPlanSchoolStudentId(screeningPlanSchoolStudent.getId());

                visionScreeningResultService.updateById(visionScreeningResult);
                return;
            }
            // 更新身份证学生的基础信息 筛查记录表的筛查学校id层级学生id 筛查学生表的层级学校id
            packageManagementStudent(student,workOrderRequestDTO);
            visionScreeningResult.setSchoolId(workOrderRequestDTO.getSchoolId())
                    .setDistrictId(school.getDistrictId())
                    .setStudentId(student.getId());
            screeningPlanSchoolStudentService.


        }


        if (StringUtils.isNotEmpty(workOrderRequestDTO.getPassport())){
            studentService.getAllByPassport(workOrderRequestDTO.getPassport());
        }

        // 工单信息修改
        WorkOrder workOrder = workOrderService.getById(workOrderRequestDTO.getWorkOrderId());
        if (Objects.isNull(workOrder)){
            throw new BusinessException("工单不存在");
        }
        workOrder.setContent(workOrderRequestDTO.getContent());
        workOrder.setStatus(WorkOrderStatusEnum.PROCESSED.code);
        workOrderService.updateById(workOrder);

    }

    /**
     * 多端学生打包
     * @param student
     * @param workOrderRequestDTO
     */
    private void packageManagementStudent(Student student, WorkOrderRequestDTO workOrderRequestDTO) {
        if (StringUtils.isNotEmpty(workOrderRequestDTO.getPassport())){
            student.setPassport(workOrderRequestDTO.getPassport());
        }
        if (StringUtils.isNotEmpty(workOrderRequestDTO.getIdCard())){
            student.setIdCard(workOrderRequestDTO.getIdCard());
        }
        if (StringUtils.isNotEmpty(workOrderRequestDTO.getSno())){
            student.setSno(workOrderRequestDTO.getSno());
        }
        student.setName(workOrderRequestDTO.getName());
        student.setGender(workOrderRequestDTO.getGender());
        student.setBirthday(workOrderRequestDTO.getBirthday());
        student.setSchoolId(workOrderRequestDTO.getSchoolId());
        student.setGradeId(workOrderRequestDTO.getGradeId());
        student.setClassId(workOrderRequestDTO.getClassId());
    }
}
