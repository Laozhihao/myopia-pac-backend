package com.wupol.myopia.business.api.school.management.domain.vo;

import com.wupol.myopia.business.aggregation.stat.domain.vo.ResultDetailVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 学校筛查结果
 *
 * @author hang.yuan 2022/9/16 20:10
 */
@Data
public class SchoolScreeningResultVO implements Serializable {

    /**
     * 筛查计划信息
     */
    private ScreeningPlanVO screeningPlan;

    /**
     * 选项tab
     */
    private List<String> tableTab;

    /**
     * 筛查数据
     */
    private ResultDetailVO resultDetailVO;
}
