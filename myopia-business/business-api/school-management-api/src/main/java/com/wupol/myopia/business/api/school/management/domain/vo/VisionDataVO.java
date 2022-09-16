package com.wupol.myopia.business.api.school.management.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

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
    @Accessors(chain = true)
    public static class VisionData implements Serializable {
        /**
         * 矫正视力
         */
        private String correctedVision;
        /**
         * 裸眼视力
         */
        private String nakedVision;

    }
}