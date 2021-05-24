package com.wupol.myopia.business.core.hospital.domain.query;

import com.wupol.myopia.business.core.hospital.domain.model.Doctor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 医生查询
 * @author Chikong
 * @date 2021-02-10
 */
@Getter
@Setter
@Accessors(chain = true)
public class DoctorQuery extends Doctor {

    /** 模糊查询 */
    private String like;

}
