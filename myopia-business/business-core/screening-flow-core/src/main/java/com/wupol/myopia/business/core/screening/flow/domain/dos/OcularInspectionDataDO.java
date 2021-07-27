package com.wupol.myopia.business.core.screening.flow.domain.dos;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 33cm眼位数据
 *
 * @Author HaoHao
 * @Date 2021/7/27
 **/
@Data
@Accessors(chain = true)
public class OcularInspectionDataDO implements Serializable {
    /**
     * 右眼数据
     */
    private OcularInspectionData rightEyeData;
    /**
     * 左眼数据
     */
    private OcularInspectionData leftEyeData;
    /**
     * 初步诊断结果：0-正常、1-（疑似）异常
     */
    private Integer diagnosis;
    /**
     * 是否配合检查：0-配合、1-不配合
     */
    private Integer isCooperative;

    @Data
    @Accessors(chain = true)
    public static class OcularInspectionData implements Serializable {
        /**
         * 0 为左眼 1 为右眼
         */
        private Integer lateriality;
        /**
         * 内斜
         */
        private Integer esotropia;
        /**
         * 外斜
         */
        private Integer exotropia;
        /**
         * 垂直位斜视
         */
        private Integer verticalStrabismus;
    }

}
