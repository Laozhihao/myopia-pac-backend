package com.wupol.myopia.business.api.management.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wupol.myopia.business.core.stat.domain.dos.KindergartenVisionAnalysisDO;
import com.wupol.myopia.business.core.stat.domain.dos.RescreenSituationDO;
import com.wupol.myopia.business.core.stat.domain.dos.ScreeningSituationDO;
import com.wupol.myopia.business.core.stat.domain.dos.FrontTableId;
import com.wupol.myopia.business.core.stat.domain.model.ScreeningResultStatistic;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * 幼儿园筛查数据结果
 *
 * @author hang.yuan 2022/4/7 17:29
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
public class KindergartenResultDetailVO implements Serializable, FrontTableId {

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


    public void setBaseData(Integer screeningNoticeId,Integer districtId,Integer screeningType, String  rangeName) {
        this.screeningType = screeningType;
        this.districtId = districtId;
        this.rangeName=rangeName;
        this.screeningNoticeId=screeningNoticeId;
    }

    public void setItemData(ScreeningResultStatistic screeningResultStatistic) {
        if (Objects.nonNull(screeningResultStatistic)){
            ScreeningSituationDO screeningSituationDO = new ScreeningSituationDO();
            BeanUtils.copyProperties(screeningResultStatistic,screeningSituationDO);

            this.screeningSituation =screeningSituationDO;
            this.kindergartenVisionAnalysis = Optional.ofNullable(screeningResultStatistic.getVisionAnalysis()).map(va -> (KindergartenVisionAnalysisDO) va).orElse(new KindergartenVisionAnalysisDO());
            this.rescreenSituation=Optional.ofNullable(screeningResultStatistic.getRescreenSituation()).orElse(new RescreenSituationDO());
        }

    }

    @Override
    public Integer getSerialVersionUID() {
        return 10;
    }
}
