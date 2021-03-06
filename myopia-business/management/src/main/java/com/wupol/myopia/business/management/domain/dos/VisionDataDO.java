package com.wupol.myopia.business.management.domain.dos;

import com.myopia.common.constant.WearingGlassesSituation;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.business.management.interfaces.ScreeningResultStructureInterface;
import com.wupol.myopia.business.management.interfaces.ValidResultDataInterface;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @Description 视力筛查结果
 * @Date 2021/1/22 16:37
 * @Author by jacob
 */
@Data
@Accessors(chain = true)
public class VisionDataDO implements ScreeningResultStructureInterface<VisionDataDO.VisionData> {
    /**
     * 右眼疾病
     */
    private VisionData rightEyeData;
    /**
     * 左眼疾病
     */
    private VisionData leftEyeData;

    @Data
    @Accessors(chain = true)
    public static class VisionData implements ValidResultDataInterface {
        /**
         * 0 为左眼 1 为右眼
         */
        private Integer lateriality;
        /**
         * 佩戴眼镜的类型： @{link com.myopia.common.constant.WearingGlassesSituation}
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
         * 判断是否有效数据
         *
         * @return
         */
        public boolean judgeValidData() {
            if (WearingGlassesSituation.NOT_WEARING_GLASSES_TYPE.equals(glassesType)) {
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

