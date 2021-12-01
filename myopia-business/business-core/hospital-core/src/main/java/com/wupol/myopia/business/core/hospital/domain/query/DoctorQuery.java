package com.wupol.myopia.business.core.hospital.domain.query;

import com.wupol.myopia.business.core.hospital.domain.dto.DoctorDTO;
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
public class DoctorQuery extends DoctorDTO {

    /** 模糊查询 */
    private String like;

}
