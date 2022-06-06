package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 血压与脊柱弯曲异常监测-表格数据
 *
 * @author hang.yuan
 * @date 2022/6/6
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BloodPressureAndSpinalCurvatureMonitorTable extends BloodPressureAndSpinalCurvatureVO {

    /**
     * 项目 （性别、学龄段、年龄段）
     */
    private String itemName;

    /**
     * 筛查人数(有效数据)
     */
    private Integer validScreeningNum;

}