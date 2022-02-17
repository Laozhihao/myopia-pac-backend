package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.business.api.management.domain.dto.StudentPreschoolCheckRecordDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.MonthAgeStatusDTO;
import com.wupol.myopia.business.core.hospital.domain.model.PreschoolCheckRecord;
import com.wupol.myopia.business.core.hospital.service.PreschoolCheckRecordService;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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

    /**
     * 获取学生信息
     * @param hospitalId
     * @param studentId
     * @return
     */
    public StudentPreschoolCheckRecordDTO getInit(Integer hospitalId, Integer studentId) {
        StudentPreschoolCheckRecordDTO record = new StudentPreschoolCheckRecordDTO();
        StudentDTO student = studentBizService.getStudentById(studentId);
        record.setStudent(student);
        // 对应当前医院各年龄段状态
        List<PreschoolCheckRecord> records = preschoolCheckRecordService.getStudentRecord(hospitalId, studentId);
        Map<Integer, MonthAgeStatusDTO> studentCheckStatus = preschoolCheckRecordService.getStudentCheckStatus(student.getBirthday(), records);
        record.setAgeStageStatusList(preschoolCheckRecordService.createMonthAgeStatusDTOByMap(studentCheckStatus));
        return record;
    }

}
