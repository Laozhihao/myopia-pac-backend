package com.wupol.myopia.business.device.api;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wupol.myopia.business.api.device.util.CheckResultUtil;
import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreenDataDTO;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;

/**
 * @Classname CheckResultUtilTest
 * @Description
 * @Date 2021/8/9 11:48 上午
 * @Author Jacob
 * @Version
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MyopiaBusinessApplication.class)
public class CheckResultUtilTest {

    /**
     * 测试总的结果
     */
    @Test
    public void getCheckResult() throws IOException {
        DeviceScreenDataDTO deviceScreenDataDTO = this.getObj("json/checkResult.json", DeviceScreenDataDTO.class);
        String checkResult = CheckResultUtil.getCheckResult(deviceScreenDataDTO);
        Assert.assertTrue("红光反射、远视、中度远视".equals(checkResult));
    }


    /**
     * 测试是否远视的判断:
     * 3岁,se超过3.5 则为远视
     */
    @Test
    public void testIsHyperopia() {
        Assert.assertTrue(CheckResultUtil.isHyperopia(36, 3.6));
    }

    /**
     * 测试是否远视级别的优先级
     */
    @Test
    public void testGetHyperopiaLevel() {
        Assert.assertTrue(CheckResultUtil.getHyperopiaLevel(36, 3.6, 3.5) == 0);
    }

    /**
     * 测试是否散光的判断:
     * cyl 绝对值大于0.5则为散光
     */
    @Test
    public void testIsAstigmia() {
        Assert.assertTrue(CheckResultUtil.isAstigmia(-0.6));
    }

    /**
     * 检查是否斜视
     * 水平、垂直斜视度数的绝对值大于8，则显示有斜视。
     *
     */
    @Test
    public void testIsStrabism() {
        Assert.assertTrue(CheckResultUtil.isStrabism(null,-9));
    }

    /**
     * 检查是否瞳孔大小不等
     * 标准: 左右瞳孔直径相差>=1
     *
     * @return
     */
    @Test
    public void testIsPR() {
        Assert.assertTrue(CheckResultUtil.isPR(9.1,8.0));
    }
    /**
     * 是否 凝视不等
     * 标准: 左右眼水平或者垂直斜视度数差的绝对值大于8
     *
     * @return
     */
    @Test
    public void testIsUnequalPupil() {
        Assert.assertTrue(CheckResultUtil.isUnequalPupil(10,8,1,10));
    }

    /**
     * 是否 屈光参差
     * 标准:
     * 1岁以下：左右眼的sph(球镜度) 或者cyl(柱镜度) 相差超过1.5D
     * 1岁以上：左右眼的sph(球镜度) 或者cyl(柱镜度) 相差超过1D
     *
     * @return
     */
    @Test
    public void testIsAnisometropia() {
        Assert.assertTrue(CheckResultUtil.isAnisometropia(9,1.8,1.0));
    }

    /**
     * @param classPathStr
     * @param clazz
     * @return
     * @throws IOException
     */
    private <T> T getObj(String classPathStr, Class<T> clazz) throws IOException {
        File file = ResourceUtils.getFile("classpath:" + classPathStr);
        String jsonStr = FileUtils.readFileToString(file);
        // 转换
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        T obj = objectMapper.readValue(jsonStr, clazz);
        return obj;
    }
}