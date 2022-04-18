package com.wupol.myopia.business.api.management.domain.vo;

import cn.hutool.core.collection.CollectionUtil;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.stat.domain.model.ScreeningResultStatistic;
import lombok.Data;

import java.util.List;
import java.util.Objects;

/**
 * 筛查结果合计详情
 *
 * @author hang.yuan 2022/4/7 17:30
 */
@Data
public class ScreeningResultStatisticDetailVO {

    /**
     * 通知id
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

    public void setBasicData(Integer districtId, String currentRangeName, ScreeningNotice screeningNotice) {
        this.districtId=districtId;
        this.rangeName=currentRangeName;
        if(screeningNotice != null){
            this.screeningType =screeningNotice.getScreeningType();
            this.screeningNoticeId=screeningNotice.getId();
        }
    }

    public void setItemData(Integer districtId,
                            List<ScreeningResultStatistic> kindergartenVisionStatistics,
                            List<ScreeningResultStatistic> primarySchoolAndAboveVisionStatistics) {
        // 下级数据 + 当前数据 + 合计数据
        if(CollectionUtil.isNotEmpty(kindergartenVisionStatistics)){
            ScreeningResultStatistic kindergartenVisionStatistic = kindergartenVisionStatistics.stream().filter(vs -> Objects.equals(districtId, vs.getDistrictId())).findFirst().orElse(null);
            if (kindergartenVisionStatistic != null){
                String statRangeName = "合计";
                this.kindergartenResultDetail = this.getKindergartenResultDetailVO(districtId, statRangeName, kindergartenVisionStatistic);
            }
        }

        if (CollectionUtil.isNotEmpty(primarySchoolAndAboveVisionStatistics)){
            ScreeningResultStatistic primarySchoolAndAboveVisionStatistic = kindergartenVisionStatistics.stream().filter(vs -> Objects.equals(districtId, vs.getDistrictId())).findFirst().orElse(null);
            if (primarySchoolAndAboveVisionStatistic != null){
                String statRangeName = "合计";
                this.primarySchoolAndAboveResultDetail = this.getPrimarySchoolAndAboveResultDetailVO(districtId, statRangeName, primarySchoolAndAboveVisionStatistic);
            }
        }

    }

    private KindergartenResultDetailVO getKindergartenResultDetailVO(Integer districtId, String statRangeName,ScreeningResultStatistic screeningResultStatistic ){

        return new KindergartenResultDetailVO();
    }
    private PrimarySchoolAndAboveResultDetailVO getPrimarySchoolAndAboveResultDetailVO(Integer districtId, String statRangeName,ScreeningResultStatistic screeningResultStatistic ){


        return new PrimarySchoolAndAboveResultDetailVO();
    }
}
