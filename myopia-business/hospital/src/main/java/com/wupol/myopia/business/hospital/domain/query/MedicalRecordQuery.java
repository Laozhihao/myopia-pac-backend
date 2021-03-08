package com.wupol.myopia.business.hospital.domain.query;

import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.business.hospital.domain.model.Doctor;
import com.wupol.myopia.business.hospital.domain.model.MedicalRecord;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 检查单查询
 * @author Chikong
 * @date 2021-02-10
 */
@Getter
@Setter
@Accessors(chain = true)
public class MedicalRecordQuery extends MedicalRecord {

    /** 开始日期 */
    private Date startDate;
    /** 结束日期 */
    private Date endDate;

}
