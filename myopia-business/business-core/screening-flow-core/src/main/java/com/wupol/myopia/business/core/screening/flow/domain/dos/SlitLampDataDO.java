package com.wupol.myopia.business.core.screening.flow.domain.dos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 裂隙灯检查数据
 *
 * @Author HaoHao
 * @Date 2021/7/27
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SlitLampDataDO extends AbstractDiagnosisResult implements Serializable {
    /**
     * 右眼数据
     */
    private SlitLampData rightEyeData;
    /**
     * 左眼数据
     */
    private SlitLampData leftEyeData;
    /**
     * 是否配合检查：0-配合、1-不配合
     */
    private Integer isCooperative;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @EqualsAndHashCode(callSuper = true)
    @Data
    @Accessors(chain = true)
    public static class SlitLampData extends AbstractDiagnosisResult implements Serializable {
        /**
         * 0 为左眼 1 为右眼
         */
        private Integer lateriality;
        /**
         * 病变眼睛组织
         */
        private List<String> pathologicalTissues;
        /**
         * 初步诊断结果：0-正常、1-（疑似）异常
         */
        private Integer diagnosis;
    }

    /**
     * 判断诊断结果是否为正常，两只眼都为正常才为正常
     *
     * @return boolean
     **/
    @Override
    public boolean isNormal() {
        return (Objects.isNull(rightEyeData) || rightEyeData.isNormal()) && (Objects.isNull(leftEyeData) || leftEyeData.isNormal());
    }

}
