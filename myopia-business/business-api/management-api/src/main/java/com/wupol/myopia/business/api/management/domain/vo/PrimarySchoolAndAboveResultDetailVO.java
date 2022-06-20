package com.wupol.myopia.business.api.management.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wupol.myopia.business.core.stat.domain.dos.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 小学及以上筛查数据结果
 *
 * @author hang.yuan 2022/4/7 17:32
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrimarySchoolAndAboveResultDetailVO implements Serializable,FrontTableId,ResultDetailVO {

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
     * 所属的通知id
     */
    private Integer screeningNoticeId;

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


    @Override
    public Integer getSerialVersionUID() {
        return 9;
    }
}
