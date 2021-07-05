package com.wupol.myopia.business.core.hospital.domain.dos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalStudent;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 医院-学生
 * @author Chikong
 * @date 2021-02-10
 */
@Data
@Accessors(chain = true)
public class HospitalStudentDO extends HospitalStudent {
    /** 医院名称 */
    private String hospitalName;
    /** 影像列表 */
    private List<String> imageUrlList;
    /** 就诊次数 */
    private Integer numOfVisits;
}
