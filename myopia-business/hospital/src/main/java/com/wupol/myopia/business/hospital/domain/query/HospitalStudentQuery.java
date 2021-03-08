package com.wupol.myopia.business.hospital.domain.query;

import com.wupol.myopia.business.hospital.domain.model.HospitalStudent;
import com.wupol.myopia.business.hospital.domain.model.MedicalReport;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 医院的学生查询
 * @author Chikong
 * @date 2021-02-10
 */
@Getter
@Setter
@Accessors(chain = true)
public class HospitalStudentQuery extends HospitalStudent {

    /** 开始日期 */
    private Date startDate;
    /** 结束日期 */
    private Date endDate;

}
