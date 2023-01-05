package com.wupol.myopia.business.util;

import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * 筛查结论计算工具类测试
 *
 * @author hang.yuan 2022/4/13 11:59
 */
public class StatUtilTest {



    @Test
    public void isLowVisionTest(){
        Assert.assertEquals(true,StatUtil.isLowVision("4.5", 2));
        Assert.assertEquals(true,StatUtil.isLowVision("4.9", 7));
        Assert.assertEquals(false,StatUtil.isLowVision("5.0", 5));
    }

    @Test
    public void getNakedVisionWarningLevelTest(){
        Assert.assertEquals(WarningLevel.THREE, StatUtil.nakedVision("4.5", 5));
        Assert.assertEquals(WarningLevel.ONE,StatUtil.nakedVision("4.9", 7));
        Assert.assertEquals(WarningLevel.THREE,StatUtil.nakedVision("4.7", 7));
        Assert.assertEquals(WarningLevel.THREE,StatUtil.nakedVision("4.4", 7));
    }

    @Test
    public void getLowVisionLevelTest(){
        Assert.assertNull(StatUtil.getLowVisionLevel(new BigDecimal("4.5"), 5));
        Assert.assertEquals(LowVisionLevelEnum.LOW_VISION_LEVEL_LIGHT,StatUtil.getLowVisionLevel(new BigDecimal("4.9"), 7));
        Assert.assertEquals(LowVisionLevelEnum.LOW_VISION_LEVEL_MIDDLE,StatUtil.getLowVisionLevel(new BigDecimal("4.7"), 7));
        Assert.assertEquals(LowVisionLevelEnum.LOW_VISION_LEVEL_HIGH,StatUtil.getLowVisionLevel(new BigDecimal("4.4"), 7));
    }

    @Test
    public void isMyopiaTest(){
        Assert.assertEquals(true,StatUtil.isMyopia(new BigDecimal("4.6"),new BigDecimal("4.6"), new BigDecimal("4.8")));
        Assert.assertEquals(true,StatUtil.isMyopia(new BigDecimal("4.6"),new BigDecimal("4.6"), new BigDecimal("4.9")));
        Assert.assertFalse(StatUtil.isMyopia(0));
        Assert.assertTrue(StatUtil.isMyopia(3));
        Assert.assertFalse(StatUtil.isMyopia(MyopiaLevelEnum.MYOPIA_LEVEL_EARLY));
    }

    @Test
    public void isRefractiveErrorTest(){
        Assert.assertEquals(true,StatUtil.isRefractiveError("2.0","3.0",2));
        Assert.assertEquals(true,StatUtil.isRefractiveError("2.0","4.0",4));
        Assert.assertEquals( true,StatUtil.isRefractiveError("3.0","4.0",6));
    }

    @Test
    public void isAnisometropiaTest(){
        Assert.assertEquals(false,StatUtil.isAnisometropiaVision("2.0","3.0"));
        Assert.assertEquals(true,StatUtil.isAnisometropiaVision("2.0","4.0"));
        Assert.assertEquals(true,StatUtil.isAnisometropiaAstigmatism("2.0","3.5"));
        Assert.assertEquals(false,StatUtil.isAnisometropiaAstigmatism("2.0","3.0"));
    }

    @Test
    public void isAstigmatismTest(){
        Assert.assertEquals(true,StatUtil.isAstigmatism("-1.0"));
        Assert.assertEquals(false,StatUtil.isAstigmatism("-0.3"));
        Assert.assertEquals(true,StatUtil.isAstigmatism(AstigmatismLevelEnum.ASTIGMATISM_LEVEL_LIGHT));
        Assert.assertEquals(AstigmatismLevelEnum.ZERO,StatUtil.getAstigmatismLevel("-0.3"));
        Assert.assertEquals(AstigmatismLevelEnum.ASTIGMATISM_LEVEL_LIGHT,StatUtil.getAstigmatismLevel("1.0"));
        Assert.assertEquals(AstigmatismLevelEnum.ASTIGMATISM_LEVEL_MIDDLE,StatUtil.getAstigmatismLevel("-3.0"));
        Assert.assertEquals(AstigmatismLevelEnum.ASTIGMATISM_LEVEL_HIGH,StatUtil.getAstigmatismLevel("-5.0"));
    }

    @Test
    public void isHyperopiaTest(){
        Assert.assertTrue(StatUtil.isHyperopia(HyperopiaLevelEnum.HYPEROPIA_LEVEL_HIGH));
        Assert.assertEquals(false,StatUtil.isHyperopia("2.0","2.0",3));
        Assert.assertEquals(true,StatUtil.isHyperopia("2.0","2.0",5));
        Assert.assertEquals(true,StatUtil.isHyperopia("2.0","2.0",7));
        Assert.assertEquals(true,StatUtil.isHyperopia("2.0","2.0",9));
    }

    @Test
    public void getSphericalEquivalentTest(){
        Assert.assertEquals(new BigDecimal("3.0"),StatUtil.getSphericalEquivalent("2.0","2.0"));
    }

    @Test
    public void nakedVisionTest(){
        Assert.assertEquals(WarningLevel.TWO,StatUtil.nakedVision("4.6",3));
        Assert.assertEquals(WarningLevel.THREE,StatUtil.nakedVision("4.6",4));
        Assert.assertEquals(WarningLevel.THREE,StatUtil.nakedVision("4.6",6));
    }

    @Test
    public void warningLevelTest(){
        Assert.assertNull(StatUtil.warningLevel(new BigDecimal("-2.00"), null, null, 0));
        Assert.assertEquals(WarningLevel.THREE,StatUtil.warningLevel(null,new BigDecimal("5.00"),null,1));
        Assert.assertNull(StatUtil.warningLevel(new BigDecimal("2.00"), null, 3, 2));
        Assert.assertNull(StatUtil.warningLevel(new BigDecimal("3.00"), null, 4, 2));
    }


    @Test
    public void isOverweightAndObesityTest(){
        Assert.assertEquals(false, Objects.requireNonNull(StatUtil.isOverweightAndObesity("58", "1.4", "10.0", 0)).getFirst());
        Assert.assertEquals(true, Objects.requireNonNull(StatUtil.isOverweightAndObesity("58", "1.4", "10.0", 0)).getSecond());
    }

    @Test
    public void malnutritionTest(){
        Assert.assertEquals(false,StatUtil.isStunting(0,"7.0","130"));
        Assert.assertEquals(false,StatUtil.isWasting("58","1.4","10.0",0));
    }

    @Test
    public void isHighBloodPressureTest(){
        Assert.assertFalse(StatUtil.isHighBloodPressure(100, 80, 0, 8,new BigDecimal("120")));
        Assert.assertFalse(StatUtil.isHighBloodPressure(100, 80, 1, 8,new BigDecimal("119")));
        Assert.assertFalse(StatUtil.isHighBloodPressure(100, 80, 0, 18,new BigDecimal("161")));
        Assert.assertFalse(StatUtil.isHighBloodPressure(100, 80, 1, 18,new BigDecimal("150")));
    }

    @Test
    public void getAgeTest(){
        Assert.assertEquals(2,StatUtil.getAge(DateUtil.toDate(LocalDate.of(2020,4,13))).getFirst().intValue());
        Assert.assertEquals("2.0",StatUtil.getAge(DateUtil.toDate(LocalDate.of(2020,4,13))).getSecond());
    }
}
