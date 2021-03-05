package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.business.management.constant.RescreeningStatisticEnum;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Random;

/**
 * @Description
 * @Date 2021/2/17 0:02
 * @Author by Jacob
 */
public class ScreeningResultSearchDTOTest {

    /**
     * 测试学校分类时
     */
    @Test
    public void testGetStatisticType_SuccessWhenGetSchoolType() {
        ScreeningResultSearchDTO screeningResultSearchDTO = new ScreeningResultSearchDTO();
        screeningResultSearchDTO.setSchoolId(new Random().nextInt());
        RescreeningStatisticEnum statisticType = screeningResultSearchDTO.getStatisticType();
        Assert.assertEquals(statisticType, RescreeningStatisticEnum.SCHOOL);
    }


    /**
     * 测试年级分类时
     */
    @Test
    public void testGetStatisticType_SuccessWhenGetGradeType() {
        ScreeningResultSearchDTO screeningResultSearchDTO = new ScreeningResultSearchDTO();
        screeningResultSearchDTO.setSchoolId(new Random().nextInt());
        screeningResultSearchDTO.setGradeName("一年级");
        RescreeningStatisticEnum statisticType = screeningResultSearchDTO.getStatisticType();
        Assert.assertEquals(statisticType, RescreeningStatisticEnum.GRADE);
    }

    /**
     * 测试班级分类时
     */
    @Test
    public void testGetStatisticType_SuccessWhenGetClazzType() {
        ScreeningResultSearchDTO screeningResultSearchDTO = new ScreeningResultSearchDTO();
        screeningResultSearchDTO.setSchoolId(new Random().nextInt());
        screeningResultSearchDTO.setGradeName("一年级");
        screeningResultSearchDTO.setClazzName("203");
        RescreeningStatisticEnum statisticType = screeningResultSearchDTO.getStatisticType();
        Assert.assertEquals(statisticType, RescreeningStatisticEnum.CLASS);
    }
}