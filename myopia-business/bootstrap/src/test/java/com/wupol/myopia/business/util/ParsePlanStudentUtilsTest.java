package com.wupol.myopia.business.util;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.device.util.ParsePlanStudentUtils;
import org.junit.Assert;
import org.junit.Test;

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

        try {
            Assert.assertEquals(Integer.valueOf(358750), ParsePlanStudentUtils.parsePlanStudentId("SAa0358750"));
        } catch (BusinessException e) {
            Assert.assertTrue(true);
            return;
        }
        Assert.fail("无法拦截异常二维码Id");
    }
}
