package com.wupol.myopia.business.core.hospital.domain.dto;

import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalStudent;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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

    /**
     * 生日信息
     */
    private String birthdayInfo;

    /**
     * 委会区域List
     */
    private List<District> committeeLists;

    /**
     * 视力筛查数
     */
    private Long reportCount;

    /**
     * 眼保健检查数
     */
    private Integer preschoolCheckCount;
}
