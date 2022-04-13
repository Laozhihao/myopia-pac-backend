package com.wupol.myopia.business.util;

import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.constant.AstigmatismLevelEnum;
import com.wupol.myopia.business.common.utils.constant.HyperopiaLevelEnum;
import com.wupol.myopia.business.common.utils.constant.MyopiaLevelEnum;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 筛查结论计算工具类测试
 *
 * @author hang.yuan 2022/4/13 11:59
 */
public class StatUtilTest {



    @Test
    public void isLowVisionTest(){
        Assert.assertEquals(StatUtil.isLowVision("4.5", 2),true);
        Assert.assertEquals(StatUtil.isLowVision("4.9", 7),true);
        Assert.assertEquals(StatUtil.isLowVision("5.0", 5),false);
    }

    @Test
    public void getNakedVisionWarningLevelTest(){
        Assert.assertEquals(StatUtil.getNakedVisionWarningLevel("4.5", 5),null);
        Assert.assertEquals(StatUtil.getNakedVisionWarningLevel("4.9", 7), WarningLevel.ONE);
        Assert.assertEquals(StatUtil.getNakedVisionWarningLevel("4.7", 7),WarningLevel.TWO);
        Assert.assertEquals(StatUtil.getNakedVisionWarningLevel("4.4", 7),WarningLevel.THREE);
    }

    @Test
    public void isMyopiaTest(){
        Assert.assertEquals(StatUtil.isMyopia(4.6f,4.6f, 4,4.8f),true);
        Assert.assertEquals(StatUtil.isMyopia(4.6f,4.6f, 7,4.9f),true);
        Assert.assertEquals(StatUtil.isMyopia(0), false);
        Assert.assertEquals(StatUtil.isMyopia(3), true);
        Assert.assertEquals(StatUtil.isMyopia(MyopiaLevelEnum.MYOPIA_LEVEL_EARLY),false);
    }

    @Test
    public void isRefractiveErrorTest(){
        Assert.assertEquals(StatUtil.isRefractiveError("2.0","3.0",2),true);
        Assert.assertEquals(StatUtil.isRefractiveError("2.0","4.0",4),true);
        Assert.assertEquals(StatUtil.isRefractiveError("3.0","4.0",6), true);
    }

    @Test
    public void isAnisometropiaTest(){
        Assert.assertEquals(StatUtil.isAnisometropiaVision("2.0","3.0"),false);
        Assert.assertEquals(StatUtil.isAnisometropiaVision("2.0","4.0"),true);
        Assert.assertEquals(StatUtil.isAnisometropiaAstigmatism("2.0","3.5"),true);
        Assert.assertEquals(StatUtil.isAnisometropiaAstigmatism("2.0","3.0"),false);
    }

    @Test
    public void isAstigmatismTest(){
        Assert.assertEquals(StatUtil.isAstigmatism("-1.0"),true);
        Assert.assertEquals(StatUtil.isAstigmatism("-0.3"),false);
        Assert.assertEquals(StatUtil.isAstigmatism(AstigmatismLevelEnum.ASTIGMATISM_LEVEL_LIGHT),true);
        Assert.assertEquals(StatUtil.getAstigmatismWarningLevel("-0.3"),AstigmatismLevelEnum.ZERO);
        Assert.assertEquals(StatUtil.getAstigmatismWarningLevel("1.0"),AstigmatismLevelEnum.ASTIGMATISM_LEVEL_LIGHT);
        Assert.assertEquals(StatUtil.getAstigmatismWarningLevel("-3.0"),AstigmatismLevelEnum.ASTIGMATISM_LEVEL_MIDDLE);
        Assert.assertEquals(StatUtil.getAstigmatismWarningLevel("-5.0"),AstigmatismLevelEnum.ASTIGMATISM_LEVEL_HIGH);
    }

    @Test
    public void isHyperopiaTest(){
        Assert.assertEquals(StatUtil.isHyperopia(HyperopiaLevelEnum.HYPEROPIA_LEVEL_HIGH),true);
        Assert.assertEquals(StatUtil.isHyperopia("2.0","2.0",3),false);
        Assert.assertEquals(StatUtil.isHyperopia("2.0","2.0",5),true);
        Assert.assertEquals(StatUtil.isHyperopia("2.0","2.0",7),true);
        Assert.assertEquals(StatUtil.isHyperopia("2.0","2.0",9),true);
    }

    @Test
    public void getSphericalEquivalentTest(){
        Assert.assertEquals(StatUtil.getSphericalEquivalent("2.0","2.0"),new BigDecimal("3.0"));
    }

    @Test
    public void nakedVisionTest(){
        Assert.assertEquals(StatUtil.nakedVision("4.6",3),WarningLevel.ONE);
        Assert.assertEquals(StatUtil.nakedVision("4.6",4),WarningLevel.TWO);
        Assert.assertEquals(StatUtil.nakedVision("4.6",6),WarningLevel.THREE);
    }

    @Test
    public void refractiveDataTest(){
        Assert.assertEquals(StatUtil.refractiveData(new BigDecimal("-2.00"),null,null,0),WarningLevel.ONE);
        Assert.assertEquals(StatUtil.refractiveData(null,new BigDecimal("5.00"),null,1),WarningLevel.THREE);
        Assert.assertEquals(StatUtil.refractiveData(new BigDecimal("2.00"),null,3,2),WarningLevel.NORMAL);
        Assert.assertEquals(StatUtil.refractiveData(new BigDecimal("3.00"),null,4,2),WarningLevel.ONE);
    }


    @Test
    public void isOverweightAndObesityTest(){
        Assert.assertEquals(StatUtil.isOverweightAndObesity("58","1.4","10.0",0).getFirst(),false);
        Assert.assertEquals(StatUtil.isOverweightAndObesity("58","1.4","10.0",0).getSecond(),true);
    }

    @Test
    public void malnutritionTest(){
        Assert.assertEquals(StatUtil.isStunting(0,"7.0","130"),false);
        Assert.assertEquals(StatUtil.isWasting("58","1.4","10.0",0),false);
    }

    @Test
    public void isHighBloodPressureTest(){
        Assert.assertEquals(StatUtil.isHighBloodPressure(100,80,0,8),false);
        Assert.assertEquals(StatUtil.isHighBloodPressure(100,80,1,8),false);
        Assert.assertEquals(StatUtil.isHighBloodPressure(100,80,0,18),false);
        Assert.assertEquals(StatUtil.isHighBloodPressure(100,80,1,18),false);
    }

    @Test
    public void getAgeTest(){
        Assert.assertEquals(StatUtil.getAge(DateUtil.toDate(LocalDate.of(2020,4,13))).getFirst().intValue(),2);
        Assert.assertEquals(StatUtil.getAge(DateUtil.toDate(LocalDate.of(2020,4,13))).getSecond(),"2.0");
    }
}
