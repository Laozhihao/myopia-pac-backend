package com.wupol.myopia.business.util;

import com.wupol.myopia.business.api.device.util.ParsePlanStudentUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Objects;

/**
 * 解析二维码工具类
 *
 * @author Simple4H
 */
public class ParsePlanStudentUtilsTest {

    @Test
    public void testQrCoedParsePlanStudentId() {
        Assert.assertEquals(Integer.valueOf(166712), ParsePlanStudentUtils.parsePlanStudentId("VS@138_166712"));
        Assert.assertEquals(Integer.valueOf(166712), ParsePlanStudentUtils.parsePlanStudentId("[VS@138_166712,166712,FM,25,null,0,null,null,null])"));
        Assert.assertEquals(Integer.valueOf(358750), ParsePlanStudentUtils.parsePlanStudentId("SA@0358750"));
    }
}
