package com.wupol.myopia.business.screening.domain.vo;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * @Description
 * @Date 2021/2/15 23:56
 * @Author by Jacob
 */
public class RescreeningGenericStructureTest {

    @Test
    public void testBuild_Success() {
        RescreeningGenericStructure rescreeningGenericStructure = RescreeningGenericStructure.getRescreeningGenericStructureBuilder()
                .setReviewRight(new BigDecimal(6))
                .setReviewLeft(new BigDecimal(1))
                .setFirstLeft(new BigDecimal(1))
                .setFirstRight(new BigDecimal(1))
                .setRangeValue(new BigDecimal(5)).build();
        Integer qualified = rescreeningGenericStructure.getQualified();
        Assert.assertTrue(RescreeningResultVO.RESCREENING_PASS.equals(qualified));
    }

    @Test
    public void testBuild_Success1() {
        RescreeningGenericStructure rescreeningGenericStructure = RescreeningGenericStructure.getRescreeningGenericStructureBuilder()
                .setReviewRight(new BigDecimal(7))
                .setReviewLeft(new BigDecimal(1))
                .setFirstLeft(new BigDecimal(1))
                .setFirstRight(new BigDecimal(1))
                .setRangeValue(new BigDecimal(5)).build();
        Integer qualified = rescreeningGenericStructure.getQualified();
        int errorTimes = rescreeningGenericStructure.getErrorTimes();
        Assert.assertTrue(errorTimes == 1);
        Assert.assertTrue(RescreeningResultVO.RESCREENING_NOT_PASS.equals(qualified));
    }
    @Test
    public void testBuild_Success3() {
        RescreeningGenericStructure rescreeningGenericStructure = RescreeningGenericStructure.getRescreeningGenericStructureBuilder()
                .setReviewRight(new BigDecimal(7))
                .setReviewLeft(new BigDecimal(7))
                .setFirstLeft(new BigDecimal(1))
                .setFirstRight(new BigDecimal(1))
                .setRangeValue(new BigDecimal(5)).build();
        Integer qualified = rescreeningGenericStructure.getQualified();
        int errorTimes = rescreeningGenericStructure.getErrorTimes();
        Assert.assertTrue(errorTimes == 2);
        Assert.assertTrue(RescreeningResultVO.RESCREENING_NOT_PASS.equals(qualified));
    }
    @Test
    public void testBuild_Success2() {
        RescreeningGenericStructure rescreeningGenericStructure = RescreeningGenericStructure.getRescreeningGenericStructureBuilder()
                .setReviewRight(new BigDecimal(3))
                .setReviewLeft(new BigDecimal(1))
                .setFirstLeft(new BigDecimal(1))
                .setFirstRight(new BigDecimal(1))
                .setRangeValue(new BigDecimal(6)).build();
        Integer qualified = rescreeningGenericStructure.getQualified();
        Assert.assertTrue(RescreeningResultVO.RESCREENING_PASS.equals(qualified));
    }

}