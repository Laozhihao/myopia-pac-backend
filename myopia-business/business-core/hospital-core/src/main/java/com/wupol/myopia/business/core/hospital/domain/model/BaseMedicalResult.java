package com.wupol.myopia.business.core.hospital.domain.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 检查的基本结果
 *
 * @Author wulizhou
 * @Date 2022/1/6 20:32
 */
@Data
@Accessors(chain = true)
public class BaseMedicalResult {

    private Integer rightStatus;
    private Integer leftStatus;
    private String rightRemark;
    private String leftRemark;
    private Integer studentId;
    private Integer doctorId;

}
