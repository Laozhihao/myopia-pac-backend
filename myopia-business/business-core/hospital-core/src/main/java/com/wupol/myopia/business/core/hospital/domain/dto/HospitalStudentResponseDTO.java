package com.wupol.myopia.business.core.hospital.domain.dto;

import com.wupol.myopia.business.core.hospital.domain.model.HospitalStudent;
import lombok.Getter;
import lombok.Setter;

/**
 * 医院-获取学生
 *
 * @author Simple4H
 */
@Getter
@Setter
public class HospitalStudentResponseDTO extends HospitalStudent {

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 年级名称
     */
    private String gradeName;
}
