package com.wupol.myopia.business.api.school.management.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 小瞳验光
 *
 * @author hang.yuan
 * @date 2022/9/13
 */
@Data
public class PupilOptometryDataVO implements Serializable {
    /**
     * 右眼数据
     */
    private PupilOptometryData rightEyeData;
    /**
     * 左眼数据
     */
    private PupilOptometryData leftEyeData;

    @Data
    @Accessors(chain = true)
    public static class PupilOptometryData implements Serializable {
        /**
         * 轴位
         */
        private String axial;
        /**
         * 球镜
         */
        private String sph;
        /**
         * 柱镜
         */
        private String cyl;
    }
}
