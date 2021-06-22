package com.wupol.myopia.business.api.screening.app.schedules;

import com.wupol.myopia.business.api.screening.app.service.ScreeningVisionMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


/**
 *  发送视力警告短信
 *
 * @author Jacob
 * @date 2021-06-08
 */
@Component
@Slf4j
public class SendWarningMsgScheduledTask {

    @Autowired
    private ScreeningVisionMsgService screeningVisionMsgService;

    /**
     * 昨天的异常vision,今天进行短信提醒;
     */
    @Scheduled(cron = "0 0 10 * * *", zone = "GMT+8:00")
    public void sendWarningMsg() {
        screeningVisionMsgService.sendWarningMsg();
    }

    /**
     * 每天检查30天前接受到异常提醒的学生的数据是否需要重新推送
     */
    @Scheduled(cron = "0 30 10 * * *", zone = "GMT+8:00")
    public void repeatNoticeWarningMsg() {
        screeningVisionMsgService.repeatNoticeWarningMsg();
    }
}