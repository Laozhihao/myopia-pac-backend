package com.wupol.myopia.business.core.screening.flow.domain.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * 学生档案卡视力详情
 *
 * @author Simple4H
 */
@Getter
@Setter
public class CardDetailsVO {

    /**
     * 佩戴眼镜的类型： @{link com.wupol.myopia.business.common.constant.WearingGlassesSituation}
     */
    private GlassesTypeObj glassesTypeObj;
    /**
     *
     * 视力检查结果
     */
    private List<VisionResult> visionResults;

    /**
     * 验光仪检查结果
     */
    private List<RefractoryResult> refractoryResults;

    /**
     * 串镜检查结果
     */
    private List<CrossMirrorResult> crossMirrorResults;

    /**
     * 其他眼病
     */
    private List<EyeDiseasesResult> eyeDiseasesResult;

    /**
     * 戴镜类型Obj
     */
    @Data
    @Accessors(chain = true)
    public static class GlassesTypeObj {
        /**
         * 待镜类型
         */
        private Integer type;
        /**
         * 左眼视力
         */
        private BigDecimal leftVision;
        /**
         * 右眼视力
         */
        private BigDecimal rightVision;
    }

    /**
     * 验光仪检查结果
     *
     * @author Simple4H
     */
    @Getter
    @Setter
    public static class RefractoryResult {

        /**
         * 0 为左眼 1 为右眼
         */
        private Integer lateriality;

        /**
         * 轴位
         */
        private BigDecimal axial;

        /**
         * 球镜
         */
        private BigDecimal sph;

        /**
         * 柱镜
         */
        private BigDecimal cyl;
    }

    /**
     * 视力检查结果
     *
     * @author Simple4H
     */
    @Getter
    @Setter
    public static class VisionResult {

        /**
         * 0 为左眼 1 为右眼
         */
        private Integer lateriality;

        /**
         * 矫正视力
         */
        private BigDecimal correctedVision;

        /**
         * 裸眼视力
         */
        private BigDecimal nakedVision;

    }

    /**
     * 串镜检查结果
     *
     * @author Simple4H
     */
    @Getter
    @Setter
    public static class CrossMirrorResult {

        /**
         * 0 为左眼 1 为右眼
         */
        private Integer lateriality;

        /**
         * 是否近视
         */
        private Boolean myopia;

        /**
         * 是否远视
         */
        private Boolean farsightedness;

        /**
         * 是否有其他
         */
        private Boolean other = false;
    }

    /**
     * 其他眼病
     *
     * @author Simple4H
     */
    @Getter
    @Setter
    public static class EyeDiseasesResult {

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
