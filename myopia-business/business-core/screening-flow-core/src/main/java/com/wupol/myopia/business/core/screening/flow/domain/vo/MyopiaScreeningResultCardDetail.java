package com.wupol.myopia.business.core.screening.flow.domain.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 学生档案卡-近视筛查结果记录表
 *
 * @author 钓猫的小鱼
 */
@Getter
@Setter
public class MyopiaScreeningResultCardDetail {
    /**
     * 佩戴眼镜的类型： @{link com.wupol.myopia.business.common.constant.WearingGlassesSituation}
     */
    private CardDetailsVO.GlassesTypeObj glassesTypeObj;
    /**
     * 筛查结果--视力检查结果
     */
    private List<CardDetailsVO.VisionResult> visionResults;

    /**
     * 验光仪检查结果
     */
    private List<CardDetailsVO.RefractoryResult> refractoryResults;

    /**
     * 扩展年龄
     */
    private String ageInfo;
    /**
     * 班主任签名
     */
    private String teacherSignature;

    /**
     * 签名图片访问地址（视力筛查）
     */
    private String visionSignPicUrl;

    /**
     * 签名图片访问地址(电脑验光)
     */
    private String computerSignPicUrl;
}
