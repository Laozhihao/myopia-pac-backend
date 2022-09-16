package com.wupol.myopia.business.api.school.management.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 电脑验光
 *
 * @author hang.yuan
 * @date 2022/9/13
 */
@Data
public class ComputerOptometryDataVO implements Serializable{

    /**
     * 左眼数据
     */
    private ComputerOptometry leftEyeData;
    /**
     * 右眼数据
     */
    private ComputerOptometry rightEyeData;

    @Data
    @Accessors(chain = true)
    public static class ComputerOptometry implements Serializable {
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