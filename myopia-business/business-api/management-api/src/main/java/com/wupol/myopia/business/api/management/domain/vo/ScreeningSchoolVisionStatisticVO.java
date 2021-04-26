package com.wupol.myopia.business.api.management.domain.vo;

import com.wupol.myopia.business.api.management.domain.dto.ScreeningBasicResult;
import com.wupol.myopia.business.core.school.constant.SchoolEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.stat.domain.model.SchoolVisionStatistic;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
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
         * 纳入统计的实际筛查学生数
         */
        private Integer validScreeningNum;

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
         * 通知id
         */
        private Integer screeningNoticeId;

        /**
         * 建议就诊数比例
         */
        private BigDecimal recommendVisitRatio;

        /**
         * 学校id
         */
        private Integer schoolId;

        /**
         * 学校id
         */
        private Integer screeningPlanId;

        /**
         * 筛查机构id
         */
        private Integer screeningOrgId;


        /**
         * 获取实例
         * @param schoolVisionStatistic
         * @param schoolDistrictName
         * @param screeningNoticeId
         * @return
         */
        public static Item getInstance(SchoolVisionStatistic schoolVisionStatistic, String schoolDistrictName, Integer screeningNoticeId) {
            Item item = new Item();
            item.screeningNum = schoolVisionStatistic.getPlanScreeningNumbers();
            item.screeningOrgName = schoolVisionStatistic.getScreeningOrgName();
            item.actualScreeningNum = schoolVisionStatistic.getRealScreeningNumbers();
            item.validScreeningNum = schoolVisionStatistic.getValidScreeningNumbers();
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
            item.recommendVisitRatio = schoolVisionStatistic.getTreatmentAdviceRatio();
            item.focusTargetsNum = schoolVisionStatistic.getFocusTargetsNumbers();
            item.screeningRangeName = schoolVisionStatistic.getSchoolName();
            item.districtName = schoolDistrictName;
            item.schoolId = schoolVisionStatistic.getSchoolId();
            item.screeningNoticeId = screeningNoticeId;
            item.screeningOrgId = schoolVisionStatistic.getScreeningOrgId();
            item.schoolType = SchoolEnum.getTypeName(schoolVisionStatistic.getSchoolType());
            item.screeningPlanId = schoolVisionStatistic.getScreeningPlanId();
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
     * @param schoolVisionStatistics
     * @param schoolIdDistrictNameMap
     * @param screeningNotice
     * @return
     */
    public static ScreeningSchoolVisionStatisticVO getInstance(List<SchoolVisionStatistic> schoolVisionStatistics, Map<Integer, String> schoolIdDistrictNameMap, ScreeningNotice screeningNotice) {
        if (CollectionUtils.isEmpty(schoolVisionStatistics)) {
            return null;
        }
        ScreeningSchoolVisionStatisticVO screeningSchoolVisionStatisticVO = new ScreeningSchoolVisionStatisticVO();
        //设置基础数据
        screeningSchoolVisionStatisticVO.setBasicData(screeningNotice);
        //设置统计数据
        screeningSchoolVisionStatisticVO.setItemData(schoolVisionStatistics,schoolIdDistrictNameMap);
        return screeningSchoolVisionStatisticVO;
    }

    /**
     * 设置item数据
     *
     * @param schoolVisionStatistics
     * @param schoolIdDistrictNameMap
     * @return
     */
    private void setItemData(List<SchoolVisionStatistic> schoolVisionStatistics, Map<Integer, String> schoolIdDistrictNameMap) {
        // 下级数据 + 当前数据 + 合计数据
        Set<Item> items = schoolVisionStatistics.stream().map(schoolVisionStatistic -> {
            Integer districtId = schoolVisionStatistic.getDistrictId();
            String schoolDistrictName = schoolIdDistrictNameMap.get(districtId);
            return Item.getInstance(schoolVisionStatistic,schoolDistrictName,getScreeningNoticeId());
                }
        ).collect(Collectors.toSet());
        contents = items;
    }


    private void setBasicData(ScreeningNotice screeningNotice) {
        super.setDataByScreeningNotice(screeningNotice);
    }
}
