package com.wupol.myopia.business.api.management.domain.bo;

import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 统计结果流转实体
 *
 * @author hang.yuan 2022/4/18 18:08
 */
@Data
@Accessors(chain = true)
public class StatisticResultBO implements Serializable {

    /**
     * 筛查通知
     */
    private ScreeningNotice screeningNotice;

    /**
     * 省级区域ID
     */
    private Integer provinceDistrictId;

    /**
     * 学校所在地区层级的计划学生总数
     */
    private Map<Integer, Long> districtPlanStudentCountMap;

    /**
     * 同一个筛查通知下不同地区筛查数据结论
     */
    private Map<Integer, List<StatConclusion>> districtStatConclusionMap;
}
