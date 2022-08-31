package com.wupol.myopia.business.core.hospital.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 对比检查单
 *
 * @author Chikong
 * @date 2021-05-19
 */
@Data
public class MedicalRecordDate implements Serializable {

    /** 检查单id */
    private Integer medicalRecordId;
    /** 检查单对应的日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createTime;
}


