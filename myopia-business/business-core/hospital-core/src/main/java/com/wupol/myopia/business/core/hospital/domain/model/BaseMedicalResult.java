package com.wupol.myopia.business.core.hospital.domain.model;

import com.wupol.myopia.business.core.hospital.domain.interfaces.HasResult;
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
public class BaseMedicalResult implements HasResult {

    private Integer rightStatus;
    private Integer leftStatus;
    private String rightRemark;
    private String leftRemark;
    private String conclusion;
    private Integer studentId;
    private Integer doctorId;
    private Boolean isAbnormal;

}
