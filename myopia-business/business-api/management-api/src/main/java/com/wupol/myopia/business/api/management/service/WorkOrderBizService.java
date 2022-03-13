package com.wupol.myopia.business.api.management.service;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.parent.domain.dto.WorkOrderDTO;
import com.wupol.myopia.business.core.parent.domain.dto.WorkOrderQueryDTO;
import com.wupol.myopia.business.core.parent.domain.dto.WorkOrderRequestDTO;
import com.wupol.myopia.business.core.parent.domain.model.WorkOrder;
import com.wupol.myopia.business.core.parent.service.WorkOrderService;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
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


    /**
     * 获取工单列表
     * @param pageRequest
     * @param workOrderQueryDTO
     * @return
     */
    public IPage<WorkOrderDTO> getWorkOrderList(PageRequest pageRequest, WorkOrderQueryDTO workOrderQueryDTO) {
        // 模糊查询学校id组装
        if (!StringUtil.isEmpty(workOrderQueryDTO.getSchoolName())){
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
    public boolean disposeOfWordOrder(WorkOrderRequestDTO workOrderRequestDTO) {
        // 对比更新信息
        StudentDTO studentDTO = studentService.getStudentById(workOrderRequestDTO.getStudentId());
        


        return false;
    }
}
