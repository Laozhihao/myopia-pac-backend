package com.wupol.myopia.business.api.management.domain.vo;

import lombok.Data;

/**
 * 筛查结果合计详情
 *
 * @author hang.yuan 2022/4/7 17:30
 */
@Data
public class ScreeningResultStatisticDetailVO {

    /**
     * 筛查类型 （0-视力筛查、1-常见病筛查）
     */
    private Integer screeningType;

    /**
     * 筛查范围、所属的地区id
     */
    private Integer districtId;

    /**
     * 筛查范围 范围名称
     */
    private String rangeName;

    /**
     * 幼儿园
     */
    private KindergartenResultDetailVO kindergartenResultDetail;

    /**
     * 小学及以上
     */
    private PrimarySchoolAndAboveResultDetailVO primarySchoolAndAboveResultDetail;
}
