package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.business.management.constant.RescreeningStatisticEnum;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 * @Description
 * @Date 2021/2/16 23:57
 * @Author by Jacob
 */
public class StudentScreeningInfoWithResultDTOTest {
    private final Integer schoolId = 1;
    private final Integer clazzId = 2;
    private final Integer gradeId = 3;

    /**
     * 获取分组key
     */
    @Test
    public void testGetGroupKey() {
        StudentScreeningInfoWithResultDTO studentScreeningInfoWithResultDTO = new StudentScreeningInfoWithResultDTO();
        studentScreeningInfoWithResultDTO.setSchoolId(schoolId);
        studentScreeningInfoWithResultDTO.setClazzId(clazzId);
        studentScreeningInfoWithResultDTO.setGradeId(gradeId);
        String groupKey = studentScreeningInfoWithResultDTO.getGroupKey(RescreeningStatisticEnum.CLASS);
        Assert.assertEquals(groupKey, schoolId + "" + gradeId + clazzId);
    }

}