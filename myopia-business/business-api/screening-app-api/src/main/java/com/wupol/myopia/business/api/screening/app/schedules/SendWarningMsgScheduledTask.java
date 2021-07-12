package com.wupol.myopia.business.api.screening.app.schedules;

import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.api.screening.app.service.ScreeningVisionMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;


/**
 *  发送视力警告短信
 *
 * @author Jacob
 * @date 2021-06-08
 */
@Component
@Slf4j
public class SendWarningMsgScheduledTask {
    /**
     * 重新检查视力异常短信的时间间隔的天数
     */
    private static final int BEFORE_30_DAYS = -30;

    @Autowired
    private ScreeningVisionMsgService screeningVisionMsgService;

    /**
     * 昨天的异常vision,今天进行短信提醒;
     */
    @Scheduled(cron = "0 0 10 * * *", zone = "GMT+8:00")
    public void sendWarningMsg() {
        //昨天10点 到 今天10点
        Date yesterdayDateTime = DateUtil.getSpecialDateTime(10,0,-1);
        Date todayDateTime = DateUtil.getTodayTime(10, 0);
        screeningVisionMsgService.sendWarningMsg(yesterdayDateTime, todayDateTime);
    }

    /**
     * 每天检查30天前接受到异常提醒的学生的数据是否需要重新推送
     */
    @Scheduled(cron = "0 30 10 * * *", zone = "GMT+8:00")
    public void repeatNoticeWarningMsg() {
        screeningVisionMsgService.repeatNoticeWarningMsg(BEFORE_30_DAYS);
    }
}