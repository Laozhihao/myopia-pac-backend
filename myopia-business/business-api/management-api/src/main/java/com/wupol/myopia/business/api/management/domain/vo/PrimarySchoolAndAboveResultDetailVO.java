package com.wupol.myopia.business.api.management.domain.vo;

import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.stat.domain.dos.*;
import com.wupol.myopia.business.core.stat.domain.model.ScreeningResultStatistic;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * 小学及以上筛查数据结果
 *
 * @author hang.yuan 2022/4/7 17:32
 */
@Data
@Accessors(chain = true)
public class PrimarySchoolAndAboveResultDetailVO implements Serializable {

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
    private PrimarySchoolAndAboveVisionAnalysisDO primarySchoolAndAboveVisionAnalysis;

    /**
     * 视力预警
     */
    private VisionWarningDO visionWarning;

    /**
     * 复测情况
     */
    private RescreenSituationDO rescreenSituation;


    /**
     * 龋齿情况
     */
    private SaprodontiaDO saprodontia;

    /**
     *  常见病分析
     */
    private CommonDiseaseDO commonDisease;

    /**
     *  问卷情况
     */
    private QuestionnaireDO questionnaire;

    public void setBaseData(ScreeningNotice screeningNotice, School school) {
        if (Objects.nonNull(screeningNotice)){
            this.screeningType=screeningNotice.getScreeningType();
        }
        if (Objects.nonNull(school)){
            this.districtId=school.getDistrictId();
            this.rangeName=school.getName();
        }
    }
    public void setBaseData(Integer screeningType, Integer districtId, String rangeName) {
        this.screeningType = screeningType;
        this.districtId = districtId;
        this.rangeName=rangeName;
    }

    public void setBaseData(Integer screeningNoticeId,Integer districtId,Integer screeningType, String  rangeName) {
        this.screeningType = screeningType;
        this.districtId = districtId;
        this.rangeName=rangeName;
        this.screeningNoticeId=screeningNoticeId;
    }

    public void setItemData(ScreeningResultStatistic screeningResultStatistic) {

        if (Objects.nonNull(screeningResultStatistic)) {
            ScreeningSituationDO screeningSituationDO = new ScreeningSituationDO();
            BeanUtils.copyProperties(screeningResultStatistic,screeningSituationDO);


            this.screeningSituation=screeningSituationDO;
            this.primarySchoolAndAboveVisionAnalysis=(PrimarySchoolAndAboveVisionAnalysisDO)screeningResultStatistic.getVisionAnalysis();
            this.visionWarning=screeningResultStatistic.getVisionWarning();
            this.rescreenSituation=screeningResultStatistic.getRescreenSituation();
            this.saprodontia=screeningResultStatistic.getSaprodontia();
            this.commonDisease=screeningResultStatistic.getCommonDisease();
            this.questionnaire=screeningResultStatistic.getQuestionnaire();
        }
    }
}
