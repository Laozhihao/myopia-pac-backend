package com.wupol.myopia.business.core.screening.flow.domain.dos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 盲及视力损害分类（等级）
 *
 * @Author HaoHao
 * @Date 2021/7/27
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Accessors(chain = true)
public class VisualLossLevelDataDO extends AbstractDiagnosisResult implements Serializable {
    /**
     * 右眼数据
     */
    private VisualLossLevelData rightEyeData;
    /**
     * 左眼数据
     */
    private VisualLossLevelData leftEyeData;
    /**
     * 是否配合检查：0-配合、1-不配合
     */
    private Integer isCooperative;

    @Data
    @Accessors(chain = true)
    public static class VisualLossLevelData implements Serializable {
        /**
         * 0 为左眼 1 为右眼
         */
        private Integer lateriality;
        /**
         * 等级：0~9 级
         */
        private Integer level;
    }

}
