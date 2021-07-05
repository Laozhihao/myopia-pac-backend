package com.wupol.myopia.business.core.screening.flow.service;

import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @Author wulizhou
 * @Date 2021/5/24 18:10
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MyopiaBusinessApplication.class)
public class StatRescreenServiceTest {

    @Autowired
    private StatRescreenService statRescreenService;

    @Test
    public void testCountByPlanAndSchool() {
        int i = statRescreenService.countByPlanAndSchool(121, 25);
        System.out.println(i);
    }

}
