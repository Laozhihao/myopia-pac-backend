package com.wupol.myopia.business.core.hospital.domain.dto;

import com.wupol.myopia.business.core.hospital.domain.dos.ReferralDO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Author wulizhou
 * @Date 2022/1/10 11:32
 */
@Data
@Accessors(chain = true)
public class HospitalStudentPreschoolCheckRecordDTO {

    /**
     * 学生信息
     */
    private HospitalStudentResponseDTO student;

    /**
     * 年龄段对应的检查的状态
     */
    private List<MonthAgeStatusDTO> ageStageStatusList;

    /**
     * 检查数据
     */
    private PreschoolCheckRecordDTO preschoolMedicalRecord;

    /**
     * 获取学生历史转诊单
     */
    private List<ReferralDO> fromReferral;

}
