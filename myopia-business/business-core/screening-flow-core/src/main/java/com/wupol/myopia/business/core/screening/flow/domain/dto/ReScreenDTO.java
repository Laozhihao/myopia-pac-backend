package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.dos.DeviationDO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 钓猫的小鱼
 * @Date: 2022/04/21/11:30
 * @Description: 复测返扩展类
 */
@Data
public class ReScreenDTO {
    /**
     * 复测次数
     */
    private Integer doubleCount;
    /**
     * 错误次数
     */
    private Integer deviationCount;
    /**
     * 复测内容（计算后）
     */
    private ReScreeningResult rescreeningResult;
    /**
     * 误差说明
     */
    private DeviationDO deviation;

    /**
     * 计算后内容（复测）
     */
    @Data
    public static class ReScreeningResult implements Serializable {
        /** 戴镜情况 */
        private String glassesTypeDesc;

        /** 裸眼（右） */
        private ScreeningDeviation rightNakedVision;

        /** 矫正（右） */
        private ScreeningDeviation rightCorrectedVision;

        /** 球镜（右） */
        private ScreeningDeviation rightSph;

        /** 柱镜（右） */
        private ScreeningDeviation rightCyl;

        /** 轴位（右） */
        private ScreeningDeviation rightAxial;

        /** 裸眼（左） */
        private ScreeningDeviation leftNakedVision;

        /** 矫正（左） */
        private ScreeningDeviation leftCorrectedVision;

        /** 球镜（左） */
        private ScreeningDeviation leftSph;

        /** 柱镜（左） */
        private ScreeningDeviation leftCyl;

        /** 轴位（左） */
        private ScreeningDeviation leftAxial;



        /** 身高 */
        private ScreeningDeviation height;

        /** 体重 */
        private ScreeningDeviation weight;

        @Data
        public static class ScreeningDeviation implements Serializable {
            /**
             * 内容
             */
            private BigDecimal content;
            /**
             * 是否错误
             */
            private boolean type;
        }

    }
}
