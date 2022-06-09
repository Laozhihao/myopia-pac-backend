package com.wupol.myopia.business.api.management.domain.vo.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 血压与脊柱弯曲异常监测-表格数据
 *
 * @author hang.yuan
 * @date 2022/6/6
 */
@Data
public class BloodPressureAndSpinalCurvatureMonitorTable  {

    /**
     * 项目 （性别、学龄段、年龄段）
     */
    private String itemName;

    /**
     * 筛查人数(有效数据)
     */
    private Integer validScreeningNum;

    /**
     * 血压偏高人数
     */
    private Integer highBloodPressureNum;

    /**
     * 脊柱弯曲异常人数
     */
    private Integer abnormalSpineCurvatureNum;

    /**
     * 血压偏高率
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal highBloodPressureRatio;

    /**
     * 脊柱弯曲异常率
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal abnormalSpineCurvatureRatio;

}