package com.wupol.myopia.business.core.hospital.domain.dto;

import com.wupol.myopia.business.core.hospital.domain.model.Doctor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 医生信息
 * @author Chikong
 * @date 2021-02-10
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class DoctorDTO extends Doctor {
    /** 报告数 */
    private Integer reportCount;
}
