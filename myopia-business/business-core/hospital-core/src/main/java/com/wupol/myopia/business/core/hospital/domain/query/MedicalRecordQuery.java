package com.wupol.myopia.business.core.hospital.domain.query;

import com.wupol.myopia.business.core.hospital.domain.model.MedicalRecord;
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
