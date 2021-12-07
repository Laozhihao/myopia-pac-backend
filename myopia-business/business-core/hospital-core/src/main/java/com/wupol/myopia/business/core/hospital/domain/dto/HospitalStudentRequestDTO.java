package com.wupol.myopia.business.core.hospital.domain.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

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
    @NotNull(message = "医院Id不能为空")
    private Integer hospitalId;

    /**
     * 姓名
     */
    private String name;
}
