package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.business.aggregation.hospital.domain.vo.HospitalStudentVO;
import com.wupol.myopia.business.aggregation.student.service.StudentFacade;
import com.wupol.myopia.business.api.management.domain.dto.StudentPreschoolCheckRecordDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalStudentResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.MonthAgeStatusDTO;
import com.wupol.myopia.business.core.hospital.domain.model.PreschoolCheckRecord;
import com.wupol.myopia.business.core.hospital.service.HospitalStudentService;
import com.wupol.myopia.business.core.hospital.service.PreschoolCheckRecordService;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Author wulizhou
 * @Date 2022/1/13 16:40
 */
@Service
public class PreschoolCheckRecordBizService {

    @Autowired
    private StudentBizService studentBizService;

    @Autowired
    private PreschoolCheckRecordService preschoolCheckRecordService;

    @Autowired
    private HospitalStudentService hospitalStudentService;
    @Autowired
    private StudentFacade studentFacade;

    /**
     * 获取学生信息
     * @param hospitalId
     * @param studentId
     * @return
     */
    public StudentPreschoolCheckRecordDTO getInit(Integer hospitalId, Integer studentId) {
        StudentPreschoolCheckRecordDTO record = new StudentPreschoolCheckRecordDTO();
        HospitalStudentVO studentVO = new HospitalStudentVO();
        // 未区分医院时，取学生信息
        if (Objects.isNull(hospitalId)) {
            StudentDTO student = studentFacade.getStudentById(studentId);
            BeanUtils.copyProperties(student, studentVO);
        } else {
            // 区分医院时，取医院下患者信息
            HospitalStudentResponseDTO student = hospitalStudentService.getByHospitalIdAndStudentId(hospitalId, studentId);
            BeanUtils.copyProperties(student, studentVO);
        }
        record.setStudent(studentVO);
        // 对应当前医院各年龄段状态
        List<PreschoolCheckRecord> records = preschoolCheckRecordService.getStudentRecord(hospitalId, studentId);
        Map<Integer, MonthAgeStatusDTO> studentCheckStatus = preschoolCheckRecordService.getStudentCheckStatus(studentVO.getBirthday(), records);
        record.setAgeStageStatusList(preschoolCheckRecordService.createMonthAgeStatusDTOByMap(studentCheckStatus));
        return record;
    }

}
