package com.wupol.myopia.business.core.screening.flow.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author 钓猫的小鱼
 * @Date 2022/4/17 17:55
 * @Email: shuailong.wu@vistel.cn
 * @Des: 自动电脑验光检查结果
 */
@Data
public class ComputerOptometryResultVO {
    /**
     * 球镜(右眼)
     */
    private BigDecimal rightSE;
    /**
     * 球镜(右眼)-复测
     */
    private BigDecimal rightSERetest;
    /**
     * 球镜(右眼)-差值
     */
    private BigDecimal rightSEDeviation;

    /**
     * 球镜(左眼)
     */
    private BigDecimal leftSE;
    /**
     * 球镜(左眼)-复测
     */
    private BigDecimal leftSERetest;
    /**
     * 球镜(左眼)-复测
     */
    private BigDecimal leftSEDeviation;
}
