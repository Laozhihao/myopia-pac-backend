package com.wupol.myopia.business.core.school.service;

import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

/**
 * @Author wulizhou
 * @Date 2021/12/6 17:36
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MyopiaBusinessApplication.class)
public class SchoolServiceTest {

    @Autowired
    private SchoolService schoolService;

    @Test
    public void testGetCooperationStopAndUnhandleSchool() {
        schoolService.getCooperationStopAndUnhandleSchool(new Date());
        Assert.assertTrue(true);
    }

}
