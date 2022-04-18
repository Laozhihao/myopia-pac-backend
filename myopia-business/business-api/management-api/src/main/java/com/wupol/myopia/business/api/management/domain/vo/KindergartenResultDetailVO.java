package com.wupol.myopia.business.api.management.domain.vo;

import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.stat.domain.dos.KindergartenVisionAnalysisDO;
import com.wupol.myopia.business.core.stat.domain.dos.RescreenSituationDO;
import com.wupol.myopia.business.core.stat.domain.dos.ScreeningSituationDO;
import com.wupol.myopia.business.core.stat.domain.model.ScreeningResultStatistic;
import lombok.Data;

import java.util.Objects;

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


    public void setBaseData(ScreeningNotice screeningNotice, School school) {
        if (Objects.nonNull(screeningNotice)){
            this.screeningType = screeningNotice.getScreeningType();
        }
        if (Objects.nonNull(school)){
            this.districtId = school.getDistrictId();
            this.rangeName=school.getName();
        }
    }

    public void setBaseData(Integer screeningNoticeId,Integer districtId,Integer screeningType, String  rangeName) {
        this.screeningType = screeningType;
        this.districtId = districtId;
        this.rangeName=rangeName;
        this.screeningNoticeId=screeningNoticeId;
    }

    public void setItemData(ScreeningResultStatistic screeningResultStatistic) {
        if (Objects.nonNull(screeningResultStatistic)){
            ScreeningSituationDO screeningSituationDO = new ScreeningSituationDO();
            screeningSituationDO.setPlanScreeningNum(screeningResultStatistic.getPlanScreeningNum())
                    .setRealScreeningNum(screeningResultStatistic.getRealScreeningNum())
                    .setFinishRatio(screeningResultStatistic.getFinishRatio())
                    .setValidScreeningNum(screeningResultStatistic.getValidScreeningNum())
                    .setValidScreeningRatio(screeningResultStatistic.getValidScreeningRatio());

            this.screeningSituation =screeningSituationDO;
            this.kindergartenVisionAnalysis = (KindergartenVisionAnalysisDO)screeningResultStatistic.getVisionAnalysis();
            this.rescreenSituation=screeningResultStatistic.getRescreenSituation();
        }

    }
}
