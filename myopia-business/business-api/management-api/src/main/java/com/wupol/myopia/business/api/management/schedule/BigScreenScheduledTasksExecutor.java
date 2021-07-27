package com.wupol.myopia.business.api.management.schedule;

import com.wupol.myopia.business.api.management.constant.BigScreeningProperties;
import com.wupol.myopia.business.api.management.service.BigScreeningStatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 大屏定时任务
 *
 * @author Jacob
 * @date 2021/05/19
 */
@Component
@Slf4j
@ConditionalOnProperty(prefix = BigScreeningProperties.PROPERTIES_PREFIX, name = "debug", havingValue = "false", matchIfMissing = true)
public class BigScreenScheduledTasksExecutor {

    @Autowired
    private BigScreeningStatService bigScreeningStatService;

    /**
     * 筛查数据统计 测试环境暂时关闭
     */
    @Scheduled(cron = "0 0 23 * * ?")
    public void statisticBigScreen() throws IOException {
        bigScreeningStatService.statisticBigScreen();
    }

}