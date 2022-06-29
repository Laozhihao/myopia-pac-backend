package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 学生筛查结果详情
 * @author tastyb
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class VisionScreeningResultDTO extends VisionScreeningResult {

    /**
     * 学生性别
     */
    private Integer gender;

    /**
     * 复测情况
     */
    private ReScreenDTO rescreening;

    /**
     * 龋齿统计
     */
    private SaprodontiaStat saprodontiaStat;

    /**
     * 等效球镜(左眼)
     */
    private BigDecimal leftSE;

    /**
     * 等效球镜(右眼)
     */
    private BigDecimal rightSE;
}
