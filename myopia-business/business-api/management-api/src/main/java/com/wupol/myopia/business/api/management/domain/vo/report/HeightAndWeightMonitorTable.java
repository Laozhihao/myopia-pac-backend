package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 身高体重监测-表格数据
 *
 * @author hang.yuan
 * @date 2022/6/6
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HeightAndWeightMonitorTable extends HeightAndWeightVO {

    /**
     * 项目 （性别、学龄段、年龄段）
     */
    private String itemName;

    /**
     * 筛查人数(有效数据)
     */
    private Integer validScreeningNum;

}