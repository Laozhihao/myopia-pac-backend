package com.wupol.myopia.business.api.management.domain.vo;

import com.wupol.myopia.business.core.stat.domain.dos.*;
import com.wupol.myopia.business.core.stat.domain.model.ScreeningResultStatistic;
import org.springframework.beans.BeanUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * 结果详情
 *
 * @author hang.yuan 2022/6/20 10:36
 */
public interface ResultDetailVO {

    default void setScreeningType(Integer screeningType){}
    default void setDistrictId(Integer districtId){}
    default void setRangeName(String rangeName){}
    default void setScreeningNoticeId(Integer screeningNoticeId){}

    default void setScreeningSituation(ScreeningSituationDO screeningSituation){}
    default void setKindergartenVisionAnalysis(KindergartenVisionAnalysisDO kindergartenVisionAnalysis){}
    default void setRescreenSituation(RescreenSituationDO rescreenSituation){}


    default void setPrimarySchoolAndAboveVisionAnalysis(PrimarySchoolAndAboveVisionAnalysisDO primarySchoolAndAboveVisionAnalysis){}
    default void setVisionWarning(VisionWarningDO visionWarning) {}
    default void setSaprodontia(SaprodontiaDO saprodontia) {}
    default void setCommonDisease(CommonDiseaseDO commonDisease) {}
    default void setQuestionnaire(QuestionnaireDO questionnaire) {}



    default void setBaseData(Integer screeningNoticeId, Integer districtId, Integer screeningType, String  rangeName) {
        setScreeningType(screeningType);
        setDistrictId(districtId);
        setRangeName(rangeName);
        setScreeningNoticeId(screeningNoticeId);
    }

    default void setItemData(ScreeningResultStatistic screeningResultStatistic,Boolean isKindergarten) {
        if (Objects.isNull(screeningResultStatistic)){
            return;
        }
        if (Objects.equals(isKindergarten,Boolean.TRUE)){
            ScreeningSituationDO screeningSituationDO = new ScreeningSituationDO();
            BeanUtils.copyProperties(screeningResultStatistic,screeningSituationDO);
            setScreeningSituation(screeningSituationDO);
            setKindergartenVisionAnalysis(Optional.ofNullable(screeningResultStatistic.getVisionAnalysis()).map(KindergartenVisionAnalysisDO.class::cast).orElse(new KindergartenVisionAnalysisDO()));
            setRescreenSituation(Optional.ofNullable(screeningResultStatistic.getRescreenSituation()).orElse(new RescreenSituationDO()));
        }

        if (Objects.equals(isKindergarten,Boolean.FALSE)) {
            ScreeningSituationDO screeningSituationDO = new ScreeningSituationDO();
            BeanUtils.copyProperties(screeningResultStatistic,screeningSituationDO);
            setScreeningSituation(screeningSituationDO);
            setPrimarySchoolAndAboveVisionAnalysis(Optional.ofNullable(screeningResultStatistic.getVisionAnalysis()).map(PrimarySchoolAndAboveVisionAnalysisDO.class::cast).orElse(new PrimarySchoolAndAboveVisionAnalysisDO()));
            setVisionWarning(Optional.ofNullable(screeningResultStatistic.getVisionWarning()).orElse(new VisionWarningDO()));
            setRescreenSituation(Optional.ofNullable(screeningResultStatistic.getRescreenSituation()).orElse(new RescreenSituationDO()));
            setSaprodontia(Optional.ofNullable(screeningResultStatistic.getSaprodontia()).orElse(new SaprodontiaDO()));
            setCommonDisease(Optional.ofNullable(screeningResultStatistic.getCommonDisease()).orElse(new CommonDiseaseDO()));
            setQuestionnaire(Optional.ofNullable(screeningResultStatistic.getQuestionnaire()).orElse(new QuestionnaireDO()));
        }

    }


}
