package com.wupol.myopia.business.api.management.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wupol.myopia.business.core.stat.domain.dos.*;
import com.wupol.myopia.business.core.stat.domain.model.ScreeningResultStatistic;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * 小学及以上筛查数据结果
 *
 * @author hang.yuan 2022/4/7 17:32
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
public class PrimarySchoolAndAboveResultDetailVO implements Serializable,FrontTableId {

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
            this.primarySchoolAndAboveVisionAnalysis= Optional.ofNullable(screeningResultStatistic.getVisionAnalysis()).map(va -> (PrimarySchoolAndAboveVisionAnalysisDO) va).orElse(new PrimarySchoolAndAboveVisionAnalysisDO());
            this.visionWarning=Optional.ofNullable(screeningResultStatistic.getVisionWarning()).orElse(new VisionWarningDO());
            this.rescreenSituation= Optional.ofNullable(screeningResultStatistic.getRescreenSituation()).orElse(new RescreenSituationDO());
            this.saprodontia=Optional.ofNullable(screeningResultStatistic.getSaprodontia()).orElse(new SaprodontiaDO());
            this.commonDisease=Optional.ofNullable(screeningResultStatistic.getCommonDisease()).orElse(new CommonDiseaseDO());
            this.questionnaire=Optional.ofNullable(screeningResultStatistic.getQuestionnaire()).orElse(new QuestionnaireDO());
        }
    }

    @Override
    public Integer getSerialVersionUID() {
        return 9;
    }
}
