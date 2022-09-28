package com.wupol.myopia.business.api.school.management.domain.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 幼儿园-学校筛查统计结果详情
 *
 * @author hang.yuan 2022/9/18 16:52
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class KindergartenSchoolStatisticVO extends VisionStatisticVO implements SchoolStatistic {


    //视力情况统计

    /**
     * 幼儿园--远视储备不足人数（默认0）
     */
    private Integer myopiaLevelInsufficientNum;

    /**
     * 幼儿园--远视储备不足率
     */
    private String myopiaLevelInsufficientRatio;

    /**
     * 幼儿园--屈光参差人数（默认0）
     */
    private Integer anisometropiaNum;

    /**
     * 幼儿园--屈光参差率
     */
    private String anisometropiaRatio;

    /**
     * 幼儿园--屈光不正人数（默认0）
     */
    private Integer ametropiaNum;

    /**
     * 幼儿园--屈光不正比例（均为整数，如10.01%，数据库则是1001）
     */
    private String ametropiaRatio;


}
