package com.wupol.myopia.business.api.screening.app.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 眼底数据
 *
 * @Author HaoHao
 * @Date 2021/7/27
 **/
@Data
public class FundusDataDTO implements Serializable {

    /**
     * 眼底(左眼)：0-未见异常、1-异常
     */
    private Integer leftHasAbnormal;
    /**
     * 眼底（右眼）：0-未见异常、1-异常
     */
    private Integer rightHasAbnormal;
    /**
     * 备注说明
     */
    private String remark;
}
