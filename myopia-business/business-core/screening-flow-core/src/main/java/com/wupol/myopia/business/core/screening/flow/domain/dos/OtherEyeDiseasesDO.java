package com.wupol.myopia.business.core.screening.flow.domain.dos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @Description 其他眼病  左右眼起码要有一只眼有疾病
 * @Date 2021/1/26 1:08
 * @Author by Jacob
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class OtherEyeDiseasesDO extends AbstractDiagnosisResult implements Serializable {
    /**
     * 右眼疾病
     */
    private OtherEyeDiseases rightEyeData;
    /**
     * 左眼疾病
     */
    private OtherEyeDiseases leftEyeData;

    @Data
    @Accessors(chain = true)
    public static class OtherEyeDiseases {
        /**
         * 0 为左眼 1 为右眼
         */
        private Integer lateriality;
        /**
         * 眼部疾病
         */
        private List<String> eyeDiseases;
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
