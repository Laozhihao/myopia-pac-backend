package com.wupol.myopia.business.bootstrap.management.schedule;

import com.wupol.myopia.business.api.management.schedule.ScheduledTasksExecutor;
import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @Author wulizhou
 * @Date 2021/12/7 15:52
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MyopiaBusinessApplication.class)
public class ScheduledTasksExecutorTest {

    @Autowired
    private ScheduledTasksExecutor scheduledTasksExecutor;

    @Test
    public void testCooperationStatusHandle() {
        scheduledTasksExecutor.cooperationStatusHandle();
    }

    @Test
    public void testCooperationWarnInfoNotice() {
        scheduledTasksExecutor.cooperationWarnInfoNotice();
    }

}
