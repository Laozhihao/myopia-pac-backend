package com.wupol.myopia.business.api.management.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.core.stat.domain.dos.KindergartenVisionAnalysisDO;
import com.wupol.myopia.business.core.stat.domain.model.ScreeningResultStatistic;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 幼儿园筛查数据结果
 *
 * @author hang.yuan 2022/4/7 17:29
 */
@Data
public class SchoolKindergartenResultVO {


    /**
     *  内容
     */
    private Set<Item> contents;


    @Data
    @Accessors(chain = true)
    public static class Item {
        /**
         * 查看的范围(地区或者学校名）
         */
        private String screeningRangeName;
        /**
         * 学校id
         */
        private Integer schoolId;

        /**
         * 筛查计划id
         */
        private Integer screeningPlanId;

        /**
         * 计划的学生数量（默认0）
         */
        private Integer planScreeningNum;

        /**
         * 实际筛查的学生数量（默认0）
         */
        private Integer realScreeningNum;

        /**
         *  实际筛查的学生比例（完成率）
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
         * 平均左眼视力（小数点后二位，默认0.0）
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal avgLeftVision;

        /**
         * 平均右眼视力（小数点后一位，默认0.0）
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
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
         * 通知id
         */
        private Integer screeningNoticeId;

        /**
         * 筛查类型
         */
        private Integer screeningType;
        /**
         * 区域ID
         */
        private Integer districtId;
        /**
         *  是否幼儿园
         */
        private Boolean isKindergarten;

        /**
         * 筛查机构
         */
        private Integer screeningOrgId;

        /**
         * 是否有复测报告
         */
        private Boolean hasRescreenReport;


    }


    public void setItemData(List<ScreeningResultStatistic> screeningResultStatistics, Map<Integer, String> schoolIdDistrictNameMap,Map<String, Boolean> hasRescreenReportMap) {
        // 下级数据 + 当前数据 + 合计数据
       this.contents = screeningResultStatistics.stream()
               .map(screeningResultStatistic -> {
                    Integer schoolId = screeningResultStatistic.getSchoolId();
                    String schoolDistrictName = schoolIdDistrictNameMap.get(schoolId);
                   return getItem(screeningResultStatistic,schoolDistrictName,screeningResultStatistic.getDistrictId(),hasRescreenReportMap);
                })
               .collect(Collectors.toSet());
    }

    private Item getItem(ScreeningResultStatistic screeningResultStatistic,
                         String schoolDistrictName,Integer districtId,
                         Map<String, Boolean> hasRescreenReportMap){
        Item item = new Item();
        BeanUtils.copyProperties(screeningResultStatistic,item);
        item.setScreeningRangeName(schoolDistrictName).setDistrictId(districtId).setIsKindergarten(Boolean.TRUE);
        if (Objects.nonNull(screeningResultStatistic.getVisionAnalysis())){
            KindergartenVisionAnalysisDO visionAnalysis = (KindergartenVisionAnalysisDO)screeningResultStatistic.getVisionAnalysis();
            BeanUtils.copyProperties(visionAnalysis,item);
        }
        Boolean hasRescreenReport = Optional.ofNullable(hasRescreenReportMap.get(screeningResultStatistic.getScreeningPlanId() + "_" + screeningResultStatistic.getSchoolId())).orElse(Boolean.FALSE);
        item.setHasRescreenReport(hasRescreenReport);

        return item;
    }
}
