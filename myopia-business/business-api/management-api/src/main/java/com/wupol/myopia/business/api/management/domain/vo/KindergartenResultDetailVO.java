package com.wupol.myopia.business.api.management.domain.vo;

import com.wupol.myopia.business.core.stat.domain.dos.KindergartenVisionAnalysisDO;
import com.wupol.myopia.business.core.stat.domain.dos.RescreenSituationDO;
import com.wupol.myopia.business.core.stat.domain.dos.ScreeningSituationDO;
import lombok.Data;

/**
 * 幼儿园筛查数据结果
 *
 * @author hang.yuan 2022/4/7 17:29
 */
@Data
public class KindergartenResultDetailVO implements SchoolResultDetailVO {

    /**
     * 所属的通知id
     */
    private Integer screeningNoticeId;

    /**
     * 筛查类型 （0-视力筛查、1-常见病筛查）
     */
    private Integer screeningType;

    /**
     * 筛查范围、所属的地区id
     */
    private Integer districtId;

    /**
     * 查看的范围(地区或者学校名）
     */
    private String rangeName;

    /**
     * 筛查情况
     */
    private ScreeningSituationDO screeningSituation;

    /**
     * 视力分析
     */
    private KindergartenVisionAnalysisDO kindergartenVisionAnalysis;

    /**
     * 复测情况
     */
    private RescreenSituationDO rescreenSituation;


}
