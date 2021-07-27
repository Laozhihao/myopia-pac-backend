package com.wupol.myopia.business.core.screening.flow.domain.dos;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @Description 其他眼病  左右眼起码要有一只眼有疾病
 * @Date 2021/1/26 1:08
 * @Author by Jacob
 */
@Data
@Accessors(chain = true)
public class OtherEyeDiseasesDO implements Serializable {
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
}
