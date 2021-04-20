package com.wupol.myopia.business.bootstrap.management;

import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 定时任务单元测试
 *
 * @author Simple4H
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MyopiaBusinessApplication.class)
public class ScheduledTasksExecutorTest {

    @Autowired
    private ScheduledTasksExecutor scheduledTasksExecutor;

    @Test
    public void sendSMSNotice() {
        scheduledTasksExecutor.sendSMSNotice();
    }
}