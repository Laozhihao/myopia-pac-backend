package com.wupol.myopia.business.core.hospital.domain.query;

import com.wupol.myopia.business.core.hospital.domain.dto.DoctorDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 医生查询
 * @author Chikong
 * @date 2021-02-10
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class DoctorQuery extends DoctorDTO {

    /** 查询 */
    private String like;

}
