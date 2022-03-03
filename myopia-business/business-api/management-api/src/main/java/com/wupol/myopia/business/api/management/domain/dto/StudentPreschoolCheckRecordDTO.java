package com.wupol.myopia.business.api.management.domain.dto;

import com.wupol.myopia.business.aggregation.hospital.domain.vo.HospitalStudentVO;
import com.wupol.myopia.business.core.hospital.domain.dto.MonthAgeStatusDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Author wulizhou
 * @Date 2022/1/10 11:32
 */
@Data
@Accessors(chain = true)
public class StudentPreschoolCheckRecordDTO {

    /**
     * 学生信息
     */
    private HospitalStudentVO student;

    /**
     * 年龄段对应的检查的状态
     */
    private List<MonthAgeStatusDTO> ageStageStatusList;

}
