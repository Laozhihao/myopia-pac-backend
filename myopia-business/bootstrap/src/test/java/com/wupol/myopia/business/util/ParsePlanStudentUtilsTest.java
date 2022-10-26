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
    public void testVsParse() {
        if (!Objects.equals(ParsePlanStudentUtils.parsePlanStudentId("VS@138_166712"), 166712)) {
            Assert.fail();
        }
        if (!Objects.equals(ParsePlanStudentUtils.parsePlanStudentId("[VS@138_166712,166712,FM,25,null,0,null,null,null])"), 166712)) {
            Assert.fail();
        }
        if (!Objects.equals(ParsePlanStudentUtils.parsePlanStudentId("SA@0358750"), 358750)) {
            Assert.fail();
        }
    }
}
