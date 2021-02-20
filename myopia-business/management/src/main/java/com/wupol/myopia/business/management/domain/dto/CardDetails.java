package com.wupol.myopia.business.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 学生档案卡视力详情
 *
 * @author Simple4H
 */
@Getter
@Setter
public class CardDetails {

    /**
     * 佩戴眼镜的类型： @{link com.myopia.common.constant.WearingGlassesSituation}
     */
    private String glassesType;
    /**
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


}
