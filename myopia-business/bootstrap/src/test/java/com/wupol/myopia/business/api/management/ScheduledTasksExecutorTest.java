package com.wupol.myopia.business.api.management;

import com.wupol.myopia.business.api.management.schedule.ScheduledTasksExecutor;
import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @Author wulizhou
 * @Date 2021/5/20 11:55
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MyopiaBusinessApplication.class)
public class ScheduledTasksExecutorTest {

    @Autowired
    private ScheduledTasksExecutor scheduledTasksExecutor;

    @Test
    public void testRescreenStat() {
        scheduledTasksExecutor.rescreenStat();
    }

}
