package com.wupol.myopia.business.api.management.domain.vo;

import com.wupol.myopia.business.core.stat.domain.dos.CommonDiseaseDO;
import com.wupol.myopia.business.core.stat.domain.dos.PrimarySchoolAndAboveVisionAnalysisDO;
import com.wupol.myopia.business.core.stat.domain.dos.SaprodontiaDO;
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
 * 小学及以上筛查数据结果
 *
 * @author hang.yuan 2022/4/7 17:32
 */
@Data
public class SchoolPrimarySchoolAndAboveResultVO {


    /**
     * 内容
     */
    private Set<Item> contents;


    @Data
    @Accessors(chain = true)
    public static class Item {
        /**
         * 学校id
         */
        private Integer schoolId;

        /**
         * 筛查计划id
         */
        private Integer screeningPlanId;

        /**
         * 查看的范围(地区或者学校名）
         */
        private String screeningRangeName;

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
         * 平均左眼视力（小数点后二位，默认0.00）
         */
        private BigDecimal avgLeftVision;

        /**
         * 平均右眼视力（小数点后二位，默认0.00）
         */
        private BigDecimal avgRightVision;

        /**
         * 建议就诊数量（默认0）
         */
        private Integer treatmentAdviceNum;

        /**
         * 建议就诊比例（均为整数，如10.01%，数据库则是1001）
         */
        private String treatmentAdviceRatio;

        /**
         * 小学及以上--近视人数（默认0）
         */
        private Integer myopiaNum;

        /**
         * 小学及以上--近视比例（均为整数，如10.01%，数据库则是1001）
         */
        private String myopiaRatio;

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

        /**
         * 区域ID
         */
        private Integer districtId;

        /**
         *  视力筛查项
         */
        private VisionItem visionItem;

        /**
         *  常见病筛查项
         */
        private CommonDiseaseItem commonDiseaseItem;


    }

    @Data
    @Accessors(chain = true)
    public static class VisionItem{
        /**
         * 小学及以上--近视前期人数（默认0）
         */
        private Integer myopiaLevelEarlyNum;

        /**
         * 小学及以上--近视前期率
         */
        private String myopiaLevelEarlyRatio;

        /**
         * 小学及以上--低度近视人数（默认0）
         */
        private Integer lowMyopiaNum;

        /**
         * 小学及以上--低度近视率
         */
        private String lowMyopiaRatio;

        /**
         * 小学及以上--高度近视人数（默认0）
         */
        private Integer highMyopiaNum;

        /**
         * 小学及以上--高度近视率
         */
        private String highMyopiaRatio;
    }

    @Data
    @Accessors(chain = true)
    public static class CommonDiseaseItem{
        /**
         * 小学及以上--龋均人数（默认0）
         */
        private Integer dmftNum;

        /**
         * 小学及以上--龋均率
         */
        private String dmftRatio;

        /**
         * 小学及以上--超重人数（默认0）
         */
        private Integer overweightNum;

        /**
         * 小学及以上--超重率
         */
        private String overweightRatio;

        /**
         * 小学及以上--肥胖人数（默认0）
         */
        private Integer obeseNum;

        /**
         * 小学及以上--肥胖率
         */
        private String obeseRatio;

        /**
         * 小学及以上--脊柱弯曲异常人数（默认0）
         */
        private Integer abnormalSpineCurvatureNum;

        /**
         * 小学及以上--脊柱弯曲异常率
         */
        private String abnormalSpineCurvatureRatio;

        /**
         * 小学及以上--血压偏高人数（默认0）
         */
        private Integer highBloodPressureNum;

        /**
         * 小学及以上--血压偏高率
         */
        private String highBloodPressureRatio;

        /**
         * 小学及以上--复查学生人数（默认0）
         */
        private Integer reviewStudentNum;

        /**
         * 小学及以上--复查学生率
         */
        private String reviewStudentRatio;
    }


    public void setItemData(List<ScreeningResultStatistic> screeningResultStatistics, Map<Integer, String> schoolIdDistrictNameMap) {
        // 下级数据
        this.contents = screeningResultStatistics.stream()
                .map(screeningResultStatistic -> {
                    Integer districtId = screeningResultStatistic.getDistrictId();
                    String schoolDistrictName = schoolIdDistrictNameMap.get(districtId);
                    return getItem(screeningResultStatistic,schoolDistrictName,districtId);
                })
                .collect(Collectors.toSet());
    }

    private Item getItem(ScreeningResultStatistic screeningResultStatistic,
                                                    String schoolDistrictName, Integer districtId){
        Item item = new Item();
        BeanUtils.copyProperties(screeningResultStatistic,item);
        item.setScreeningRangeName(schoolDistrictName).setDistrictId(districtId).setIsKindergarten(Boolean.FALSE);

        PrimarySchoolAndAboveVisionAnalysisDO visionAnalysis = (PrimarySchoolAndAboveVisionAnalysisDO)screeningResultStatistic.getVisionAnalysis();
        BeanUtils.copyProperties(visionAnalysis,item);

        if (Objects.equals(0,screeningResultStatistic.getScreeningType())){
            VisionItem visionItem = new VisionItem();
            BeanUtils.copyProperties(visionAnalysis,visionItem);
            item.setVisionItem(visionItem);
        }else {
            SaprodontiaDO saprodontia = screeningResultStatistic.getSaprodontia();
            CommonDiseaseDO commonDisease = screeningResultStatistic.getCommonDisease();
            CommonDiseaseItem commonDiseaseItem= new CommonDiseaseItem();
            commonDiseaseItem.setDmftNum(saprodontia.getDmftNum())
                    .setDmftRatio(saprodontia.getDmftRatio());
            BeanUtils.copyProperties(commonDisease,commonDiseaseItem);
            item.setCommonDiseaseItem(commonDiseaseItem);
        }

        return item;
    }
}
