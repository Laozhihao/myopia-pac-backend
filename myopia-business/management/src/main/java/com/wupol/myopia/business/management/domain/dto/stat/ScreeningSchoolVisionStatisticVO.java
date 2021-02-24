package com.wupol.myopia.business.management.domain.dto.stat;

import com.wupol.myopia.business.management.constant.SchoolEnum;
import com.wupol.myopia.business.management.domain.model.SchoolVisionStatistic;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class ScreeningSchoolVisionStatisticVO extends ScreeningBasicResult {

    /**
     * 内容
     */
    private Set<Item> contents;

    @Getter
    @Accessors(chain = true)
    public static class Item {
        /**
         * 查看的范围(地区或者学校名）
         */
        private String screeningRangeName;

        /**
         * 行政区域
         */
        private String districtName;

        /**
         * 私有构造方法
         */
        private Item() {

        }

        /**
         * 筛查机构名称
         */
        private String screeningOrgName;
        /**
         * 学校类型
         */
        private String schoolType;
        /**
         * 筛查学生数
         */
        private Integer screeningNum;

        /**
         * 实际筛查学生数
         */
        private Integer actualScreeningNum;

        /**
         * 左眼平均视力
         */
        private BigDecimal averageVisionLeft;

        /**
         * 右眼平均视力
         */
        private BigDecimal averageVisionRight;

        /**
         * 视力低下率
         */
        private BigDecimal lowVisionRatio;

        /**
         * 视力低下人数
         */
        private Integer lowVisionNum;
        /**
         * 屈光不正率
         */
        private BigDecimal refractiveErrorRatio;

        /**
         * 屈光不正人数
         */
        private Integer refractiveErrorNum;

        /**
         * 戴镜率
         */
        private BigDecimal wearingGlassesRatio;
        /**
         * 戴镜人数
         */
        private Integer wearingGlassesNum;
        /**
         * 近视人数
         */
        private Integer myopiaNum;

        /**
         * 近视率
         */
        private BigDecimal myopiaRatio;

        /**
         * 0级预警率
         */
        private BigDecimal warningLevelZeroRatio;

        /**
         * 0级预警人数
         */
        private Integer warningLevelZeroNum;

        /**
         * 1级预警率
         */
        private BigDecimal warningLevelOneRatio;

        /**
         * 1级预警人数
         */
        private Integer warningLevelOneNum;

        /**
         * 2级预警率
         */
        private BigDecimal warningLevelTwoRatio;

        /**
         * 2级预警人数
         */
        private Integer warningLevelTwoNum;

        /**
         * 3级预警率
         */
        private BigDecimal warningLevelThreeRatio;

        /**
         * 3级预警人数
         */
        private Integer warningLevelThreeNum;

        /**
         * 重点视力对象数量
         */
        private Integer focusTargetsNum;

        /**
         * 建议就诊数
         */
        private Integer recommendVisitNum;

        /**
         * 获取实例
         * @param schoolVisionStatistic
         * @param districtName
         * @return
         */
        public static Item getInstance(SchoolVisionStatistic schoolVisionStatistic, String districtName) {
            Item item = new Item();
            item.screeningNum = schoolVisionStatistic.getPlanScreeningNumbers();
            item.screeningOrgName = schoolVisionStatistic.getScreeningOrgName();
            item.actualScreeningNum = schoolVisionStatistic.getRealScreeningNumners();
            item.averageVisionLeft = schoolVisionStatistic.getAvgLeftVision();
            item.averageVisionRight = schoolVisionStatistic.getAvgRightVision();
            item.lowVisionNum = schoolVisionStatistic.getLowVisionNumbers();
            item.lowVisionRatio = schoolVisionStatistic.getLowVisionRatio();
            item.warningLevelZeroNum = schoolVisionStatistic.getVisionLabel0Numbers();
            item.warningLevelZeroRatio = schoolVisionStatistic.getVisionLabel0Ratio();
            item.warningLevelOneNum = schoolVisionStatistic.getVisionLabel1Numbers();
            item.warningLevelOneRatio = schoolVisionStatistic.getVisionLabel1Ratio();
            item.warningLevelTwoNum = schoolVisionStatistic.getVisionLabel2Numbers();
            item.warningLevelTwoRatio = schoolVisionStatistic.getVisionLabel2Ratio();
            item.warningLevelThreeNum = schoolVisionStatistic.getVisionLabel3Numbers();
            item.warningLevelThreeRatio = schoolVisionStatistic.getVisionLabel3Ratio();
            item.wearingGlassesNum = schoolVisionStatistic.getWearingGlassesNumbers();
            item.wearingGlassesRatio = schoolVisionStatistic.getWearingGlassesRatio();
            item.myopiaNum = schoolVisionStatistic.getMyopiaNumbers();
            item.myopiaRatio = schoolVisionStatistic.getMyopiaRatio();
            item.refractiveErrorNum = schoolVisionStatistic.getAmetropiaNumbers();
            item.refractiveErrorRatio = schoolVisionStatistic.getAmetropiaRatio();
            item.recommendVisitNum = schoolVisionStatistic.getTreatmentAdviceNumbers();
            item.focusTargetsNum = schoolVisionStatistic.getFocusTargetsNumbers();
            item.screeningRangeName = schoolVisionStatistic.getSchoolName();
            item.districtName = districtName;
            item.schoolType = SchoolEnum.getTypeName(schoolVisionStatistic.getSchoolType());
            return item;
        }
    }


    public static ScreeningSchoolVisionStatisticVO getEmptyInstance() {
        ScreeningSchoolVisionStatisticVO screeningSchoolVisionStatisticVO = new ScreeningSchoolVisionStatisticVO();
        return screeningSchoolVisionStatisticVO;
    }

    /**
     * 获取实例
     *
     * @param districtName
     * @param schoolVisionStatistics
     * @param screeningNotice
     * @return
     */
    public static ScreeningSchoolVisionStatisticVO getInstance(List<SchoolVisionStatistic> schoolVisionStatistics, String districtName, ScreeningNotice screeningNotice) {
        if (CollectionUtils.isEmpty(schoolVisionStatistics)) {
            return null;
        }
        ScreeningSchoolVisionStatisticVO screeningSchoolVisionStatisticVO = new ScreeningSchoolVisionStatisticVO();
        //设置基础数据
        screeningSchoolVisionStatisticVO.setBasicData(screeningNotice);
        //设置统计数据
        screeningSchoolVisionStatisticVO.setItemData(schoolVisionStatistics,districtName);
        return screeningSchoolVisionStatisticVO;
    }

    /**
     * 设置item数据
     *
     * @param schoolVisionStatistics
     * @param districtName
     * @return
     */
    private void setItemData(List<SchoolVisionStatistic> schoolVisionStatistics, String districtName) {
        // 下级数据 + 当前数据 + 合计数据
        Set<ScreeningSchoolVisionStatisticVO.Item> items = schoolVisionStatistics.stream().map(schoolVisionStatistic ->
                Item.getInstance(schoolVisionStatistic,districtName)
        ).collect(Collectors.toSet());
        contents = items;
    }


    private void setBasicData(ScreeningNotice screeningNotice) {
        super.setDataByScreeningNotice(screeningNotice);
    }
}