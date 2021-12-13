package com.wupol.myopia.business.core.system.service;

import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @Author wulizhou
 * @Date 2021/12/8 16:56
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MyopiaBusinessApplication.class)
public class NoticeServiceTest {

    @Autowired
    private NoticeService noticeService;

    @Test
    public void testSendNoticeToAllAdmin() {
        noticeService.sendNoticeToAllAdmin(-1, "这里一个伟大的通知", "这是一个伟大的内容", CommonConst.NOTICE_STATION_LETTER);
    }

}
