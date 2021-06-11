package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

/**
 * @Author wulizhou
 * @Date 2021/6/11 17:54
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MyopiaBusinessApplication.class)
public class ScreeningTaskBizServiceTest {

    @Autowired
    private ScreeningTaskBizService screeningTaskBizService;

    @Test
    public void testGetScreeningPlanByUser() {
        CurrentUser user = new CurrentUser();
        user.setId(5);
        user.setOrgId(1);
        user.setSystemCode(1);
        user.setUsername("wlz");
        user.setRoleTypes(Arrays.asList(0));
        List<ScreeningTask> tasks = screeningTaskBizService.getScreeningPlanByUser(user);
        System.out.println(tasks);
    }

}
