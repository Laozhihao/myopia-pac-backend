package com.wupol.myopia.business.api.management.domain.vo;

import com.wupol.myopia.business.core.stat.domain.dos.KindergartenVisionAnalysisDO;
import com.wupol.myopia.business.core.stat.domain.model.ScreeningResultStatistic;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 幼儿园筛查数据结果
 *
 * @author hang.yuan 2022/4/7 17:29
 */
@Data
public class KindergartenResultVO {


    /**
     * 筛查范围、所属的地区id
     */
    private Integer districtId;

    /**
     * 筛查范围 范围名称
     */
    private String rangeName;

    /**
     * 合计数据
     */
    private Item totalData;

    /**
     * 当前级的数据
     */
    private Item currentData;

    /**
     * 下级的数据列表，如果没有的话，为null
     */
    private Set<Item> childDataSet;



    @Data
    @Accessors(chain = true)
    public static class Item {
        /**
         * 查看的范围(地区或者学校名）
         */
        private String screeningRangeName;
        /**
         * 学校数
         */
        private Integer schoolNum;
        /**
         * 计划的学生数量（默认0）
         */
        private Integer planScreeningNum;

        /**
         * 实际筛查的学生数量（默认0）
         */
        private Integer realScreeningNum;
        /**
         * 实际筛查的学生比例（完成率）
         */
        private String finishRatio;

        /**
         * 纳入统计的实际筛查学生数量（默认0）
         */
        private Integer validScreeningNum;
        /**
         * 纳入统计的实际筛查学生比例
         */
        private String validScreeningRatio;

        /**
         * 视力低下人数（默认0）
         */
        private Integer lowVisionNum;

        /**
         * 视力低下比例（均为整数，如10.01%，数据库则是1001）
         */
        private String lowVisionRatio;

        /**
         * 平均左眼视力（小数点后二位，默认0.00）
         */
        private BigDecimal avgLeftVision;

        /**
         * 平均右眼视力（小数点后二位，默认0.00）
         */
        private BigDecimal avgRightVision;

        /**
         * 幼儿园--屈光不正人数（默认0）
         */
        private Integer ametropiaNum;

        /**
         * 幼儿园--屈光不正比例（均为整数，如10.01%，数据库则是1001）
         */
        private String ametropiaRatio;

        /**
         * 幼儿园--屈光参差人数（默认0）
         */
        private Integer anisometropiaNum;

        /**
         * 幼儿园--屈光参差率
         */
        private String anisometropiaRatio;

        /**
         * 幼儿园--远视储备不足人数（默认0）
         */
        private Integer myopiaLevelInsufficientNum;

        /**
         * 幼儿园--远视储备不足率
         */
        private String myopiaLevelInsufficientRatio;

        /**
         * 建议就诊数量（默认0）
         */
        private Integer treatmentAdviceNum;

        /**
         * 建议就诊比例（均为整数，如10.01%，数据库则是1001）
         */
        private String treatmentAdviceRatio;

        /**
         * 区域ID
         */
        private Integer districtId;

        /**
         * 通知id
         */
        private Integer screeningNoticeId;

        /**
         * 筛查类型
         */
        private Integer screeningType;

        /**
         *  是否幼儿园
         */
        private Boolean isKindergarten;


    }


    public void setBasicData(Integer districtId, String currentRangeName) {
        this.districtId = districtId;
        this.rangeName = currentRangeName;
    }

    public void setCurrentData(ScreeningResultStatistic currentVisionStatistic){
        if (Objects.isNull(currentVisionStatistic)){return;}
        this.currentData = this.getItem(districtId,getRangeName(),currentVisionStatistic);
    }

    private Item getItem(Integer districtId,String rangeName, ScreeningResultStatistic currentVisionStatistic) {
        Item item = new Item();
        BeanUtils.copyProperties(currentVisionStatistic,item);
        item.setScreeningRangeName(rangeName).setDistrictId(districtId).setIsKindergarten(Boolean.TRUE);

        if (Objects.nonNull(currentVisionStatistic.getVisionAnalysis())){
            KindergartenVisionAnalysisDO visionAnalysis = (KindergartenVisionAnalysisDO)currentVisionStatistic.getVisionAnalysis();
            BeanUtils.copyProperties(visionAnalysis,item);
        }

        return item;

    }

    public void setItemData(Integer districtId, List<ScreeningResultStatistic> visionStatistics, Map<Integer, String> districtIdNameMap) {
        // 下级数据 + 当前数据 + 合计数据
        this.childDataSet = visionStatistics.stream().map(visionStatistic->{
            Integer statDistrictId = visionStatistic.getDistrictId();
            String statRangeName;
            //是合计数据
            if (Objects.equals(districtId,statDistrictId)) {
                statRangeName = "合计";
                this.totalData = this.getItem(statDistrictId, statRangeName, visionStatistic);
                return null;
            }
            statRangeName = districtIdNameMap.get(statDistrictId);
            return this.getItem(statDistrictId, statRangeName, visionStatistic);
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }



}
