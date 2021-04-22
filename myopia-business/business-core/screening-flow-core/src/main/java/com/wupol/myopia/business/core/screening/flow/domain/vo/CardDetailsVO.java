package com.wupol.myopia.business.core.screening.flow.domain.vo;

import com.wupol.myopia.business.core.screening.flow.domain.dos.CrossMirrorResultDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.EyeDiseasesResultDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.RefractoryResultDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionResultDO;
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
     * 视力检查结果
     */
    private List<VisionResultDO> visionResultDOS;

    /**
     * 验光仪检查结果
     */
    private List<RefractoryResultDO> refractoryResultDOS;

    /**
     * 串镜检查结果
     */
    private List<CrossMirrorResultDO> crossMirrorResultDOS;

    /**
     * 其他眼病
     */
    private List<EyeDiseasesResultDO> eyeDiseasesResultDO;

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
}
