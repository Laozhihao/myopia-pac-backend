package com.wupol.myopia.business.api.school.management.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
import com.wupol.myopia.business.common.utils.interfaces.ValidResultDataInterface;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 视力数据
 *
 * @author hang.yuan
 * @date 2022/9/13
 */
@Data
public class VisionDataVO {
    /**
     * 戴镜类型
     */
    private Integer glassesType;
    /**
     * 左眼数据
     */
    private VisionData leftEyeData;
    /**
     * 右眼数据
     */
    private VisionData rightEyeData;

    @Data
    public static class VisionData implements Serializable {
        /**
         * 矫正视力
         */
        private BigDecimal correctedVision;
        /**
         * 裸眼视力
         */
        private BigDecimal nakedVision;

    }
}