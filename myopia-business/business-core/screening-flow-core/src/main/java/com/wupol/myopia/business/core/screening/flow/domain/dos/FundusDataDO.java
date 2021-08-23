package com.wupol.myopia.business.core.screening.flow.domain.dos;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 眼底数据
 *
 * @Author HaoHao
 * @Date 2021/7/27
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class FundusDataDO extends AbstractDiagnosisResult implements Serializable {
    /**
     * 右眼数据
     */
    private FundusData rightEyeData;
    /**
     * 左眼数据
     */
    private FundusData leftEyeData;
    /**
     * 备注说明
     */
    private String remark;
    /**
     * 是否配合检查：0-配合、1-不配合
     */
    private Integer isCooperative;

    @Data
    @Accessors(chain = true)
    public static class FundusData implements Serializable {
        /**
         * 0 为左眼 1 为右眼
         */
        private Integer lateriality;
        /**
         * 眼底：0-未见异常、1-异常
         */
        private Integer hasAbnormal;
    }

    /**
     * 判断诊断结果是否为正常，筛查APP没有录入初诊结果，故默认为正常
     *
     * @return boolean
     **/
    @Override
    public boolean isNormal() {
        return true;
    }

}
