package com.wupol.myopia.business.core.screening.flow.domain.dos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
import com.wupol.myopia.business.common.utils.interfaces.ScreeningResultStructureInterface;
import com.wupol.myopia.business.common.utils.interfaces.ValidResultDataInterface;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description 视力筛查结果
 * @Date 2021/1/22 16:37
 * @Author by jacob
 */
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Accessors(chain = true)
public class VisionDataDO extends AbstractDiagnosisResult implements ScreeningResultStructureInterface<VisionDataDO.VisionData>, Serializable {
    /**
     * 右眼疾病
     */
    private VisionData rightEyeData;
    /**
     * 左眼疾病
     */
    private VisionData leftEyeData;
    /**
     * 是否配合检查：0-配合、1-不配合
     */
    private Integer isCooperative;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    @Accessors(chain = true)
    public static class VisionData implements ValidResultDataInterface,Serializable {
        /**
         * 0 为左眼 1 为右眼
         */
        private Integer lateriality;
        /**
         * 佩戴眼镜的类型： {@link com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation}
         */
        private Integer glassesType;
        /**
         * 矫正视力
         */
        private BigDecimal correctedVision;
        /**
         * 裸眼视力
         */
        private BigDecimal nakedVision;

        /**
         * 夜戴角膜的度数
         */
        private BigDecimal okDegree;

        /**
         * 判断是否有效数据
         *
         * @return
         */
        @Override
        public boolean judgeValidData() {
            if (WearingGlassesSituation.NOT_WEARING_GLASSES_KEY.equals(glassesType)) {
                return nakedVision != null;
            }
            return nakedVision != null && correctedVision != null;
        }
    }

    /**
     * 判断会否有完整的裸眼视力数据
     * @return
     */
    public boolean validCorrectedVision() {
        return ObjectsUtil.allNotNull(getLeftEyeData()
                , getLeftEyeData().getCorrectedVision()
                , getRightEyeData(), getRightEyeData().getCorrectedVision());
    }

    /**
     * 判断会否有完整的矫正视力
     * @return
     */
    public boolean validNakedVision() {
        return ObjectsUtil.allNotNull(getLeftEyeData()
                , getLeftEyeData().getNakedVision()
                , getRightEyeData(), getRightEyeData().getNakedVision());
    }

}

