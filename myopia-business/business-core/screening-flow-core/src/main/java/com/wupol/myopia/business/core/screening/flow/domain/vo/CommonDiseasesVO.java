package com.wupol.myopia.business.core.screening.flow.domain.vo;

import com.wupol.myopia.business.core.screening.flow.domain.dos.DeviationDO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author 钓猫的小鱼
 * @Date 2022/4/17 17:42
 * @Email: shuailong.wu@vistel.cn
 * @Des: 常见病扩展类
 */
@Data
public class CommonDiseasesVO {
    /**
     * 身高和体格检查误差卡片
     */
    private HeightAndWeightResult heightAndWeightResult;

    /**
     * 质控人员
     */
    private String qualityControlName;
    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 身高和体格检查误差卡片
     */
    @Data
    public static class HeightAndWeightResult implements Serializable {
        /**
         * 身高
         */
        private BigDecimal height;
        /**
         * 身高-复测
         */
        private BigDecimal heightReScreen;
        /**
         * 身高-误差
         */
        private BigDecimal heightDeviation;
        /**
         * 体重
         */
        private BigDecimal weight;
        /**
         * 体重-复测
         */
        private BigDecimal weightReScreen;
        /**
         * 体重-误差
         */
        private BigDecimal weightDeviation;

        /**
         * 身高体重-误差原因
         */
        private DeviationDO.HeightWeightDeviation heightWeightDeviation;

    }
}
