package com.wupol.myopia.business.core.hospital.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 对比检查单
 *
 * @author Chikong
 * @date 2021-05-19
 */
@Data
public class MedicalRecordDate {

    /** 检查单id */
    private Integer medicalRecordId;
    /** 检查单对应的日期 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date createTime;
}


