package com.wupol.myopia.business.core.hospital.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 医院-获取学生request
 *
 * @author Simple4H
 */
@Getter
@Setter
public class HospitalStudentRequestDTO {

    /**
     * 医院Id
     */
    private Integer hospitalId;

    /**
     * 姓名
     */
    private String name;
}
