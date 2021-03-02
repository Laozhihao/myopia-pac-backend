package com.wupol.myopia.business.management.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Alix
 * @date 2021/02/19
 */
@Component
@Slf4j
public class ScheduledTasksExecutor {

    /**
     * 筛查数据统计
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void statistic() {
        //1. 查询出需要统计的通知
        //2. 分别处理每个通知
        //2.1 查询出通知对应的筛查数据(结果)
        //2.2 学校、层级维度统计
        //2.3 某个地区层级最新统计的重点视力对象情况以及所有上级的情况（）
    }
}