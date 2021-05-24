package com.wupol.myopia.business.api.screening.app.domain.vo;

import com.wupol.myopia.business.api.screening.app.interfaces.JudgePassInterfaces;
import com.wupol.myopia.business.common.utils.exception.ManagementUncheckedException;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description
 * @Date 2021/1/29 14:34
 * @Author by Jacob
 */
@Data
public class RescreeningGenericStructure implements JudgePassInterfaces {

    private RescreeningGenericStructure() {

    }

    private BigDecimal firstRight;
    /**
     * firstLeft
     */
    private BigDecimal firstLeft;
    /**
     * reviewRight
     */
    private BigDecimal reviewRight;
    /**
     * reviewLeft
     */
    private BigDecimal reviewLeft;
    /**
     * qualified
     */
    private Integer qualified;

    /**
     * 错误项 做多2次
     */
    private int errorTimes;


    public void addErrorTimes() {
        errorTimes++;
    }

    @Override
    public boolean judgePass() {
        return false;
    }

    /**
     * 获取构建器
     *
     * @return
     */
    public static RescreeningGenericStructureBuilder getRescreeningGenericStructureBuilder() {
        return new RescreeningGenericStructureBuilder();
    }

    public static class RescreeningGenericStructureBuilder {

        private final RescreeningGenericStructure rescreeningGenericStructure = new RescreeningGenericStructure();
        private BigDecimal rangeValue;
        public RescreeningGenericStructureBuilder setFirstRight(BigDecimal firstRight) {
            rescreeningGenericStructure.setFirstRight(firstRight);
            return this;
        }

        public RescreeningGenericStructureBuilder setFirstLeft(BigDecimal firstLeft) {
            rescreeningGenericStructure.setFirstLeft(firstLeft);
            return this;
        }

        public RescreeningGenericStructureBuilder setReviewLeft(BigDecimal reviewLeft) {
            rescreeningGenericStructure.setReviewLeft(reviewLeft);
            return this;
        }

        public RescreeningGenericStructureBuilder setReviewRight(BigDecimal reviewRight) {
            rescreeningGenericStructure.setReviewRight(reviewRight);
            return this;
        }

        public RescreeningGenericStructureBuilder setRangeValue(BigDecimal rangeValue) {
            if (rangeValue == null) {
                throw new ManagementUncheckedException("rangeValue 不能为空");
            }
            this.rangeValue = rangeValue.abs();
            return this;
        }

        /**
         * 计算qualified 不完整的数据，不给显示
         *
         * @return
         */
        public RescreeningGenericStructure build() {
            if (rescreeningGenericStructure.getFirstRight() == null || rescreeningGenericStructure.getReviewRight() == null || rescreeningGenericStructure.getReviewLeft() == null || rescreeningGenericStructure.getFirstLeft() == null) {
                //表示数据不完整
                throw new ManagementUncheckedException("setQualified错误，数据不完整");
            }
            BigDecimal rightValue = rescreeningGenericStructure.getFirstRight().subtract(rescreeningGenericStructure.getReviewRight()).abs();
            BigDecimal leftValue = rescreeningGenericStructure.getFirstLeft().subtract(rescreeningGenericStructure.getReviewLeft()).abs();
            //属于误差范围内
            rescreeningGenericStructure.setQualified(RescreeningResultVO.RESCREENING_PASS);
            if (rightValue.compareTo(rangeValue) > 0) {
                rescreeningGenericStructure.addErrorTimes();
                rescreeningGenericStructure.setQualified(RescreeningResultVO.RESCREENING_NOT_PASS);
            }

            if (leftValue.compareTo(rangeValue) > 0) {
                rescreeningGenericStructure.addErrorTimes();
                rescreeningGenericStructure.setQualified(RescreeningResultVO.RESCREENING_NOT_PASS);
            }
            return rescreeningGenericStructure;
        }
    }


}