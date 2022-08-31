package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.aggregation.screening.service.StatConclusionBizService;
import com.wupol.myopia.business.api.management.schedule.StatisticScheduledTaskService;
import com.wupol.myopia.business.api.management.service.BigScreeningStatService;
import com.wupol.myopia.business.core.stat.service.ScreeningResultStatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * 运维操作控制层
 * 1、为了测试方便
 * 2、处理其他后台数据转换接口
 *
 * @author hang.yuan 2022/6/21 19:03
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/test")
public class OperationAndMaintenanceController {

    @Autowired
    private BigScreeningStatService bigScreeningStatService;
    @Autowired
    private StatConclusionBizService statConclusionBizService;
    @Autowired
    private ScreeningResultStatisticService screeningResultStatisticService;
    @Autowired
    private ThreadPoolTaskExecutor asyncServiceExecutor;
    @Autowired
    private StatisticScheduledTaskService statisticScheduledTaskService;

    /**
     * 触发大屏统计
     *
     * @throws IOException
     */
    @GetMapping("/big")
    public void statBigScreen() {
        bigScreeningStatService.statisticBigScreen();
    }

    /**
     * 筛查结果数据转筛查数据结论和筛查结果统计（便于测试今天的筛查结果统计数据）
     *
     * @param planId 筛查计划ID, 不必填
     * @param noticeId 筛查通知ID, 不必填
     * @param isAll 是否全部 (true-全部,false-不是全部) 必填
     * @param exclude 排查的计划ID，多个用逗号隔开，不必填
     */
    @GetMapping("screeningToConclusion")
    @Async
    public void afreshConclusionAndStatistic(@RequestParam(required = false) Integer planId,
                                             @RequestParam(required = false) Integer noticeId,
                                             @RequestParam Boolean isAll,
                                             @RequestParam(required = false) String exclude) {
        statConclusionBizService.screeningToConclusion(planId, isAll, exclude);
        statisticScheduledTaskService.statistic(null, planId, isAll, exclude, noticeId);
    }

    /**
     * 筛查结果转换筛查结论数据，解决了录入筛查结果数据正常，筛查结论数据有误，修改规则后可以更新结论数据
     */
    @GetMapping("afreshScreeningToConclusion")
    public void afreshConclusion(Integer planId){
        CompletableFuture.runAsync(()-> statConclusionBizService.screeningToConclusion(planId,Boolean.FALSE,null),asyncServiceExecutor);
    }

    /**
     * 筛查结果统计，根据筛查计划删除旧数据重新生成，解决修改数据之后，统计数据存在旧数据(脏数据或废弃数据)问题
     */
    @GetMapping("afreshStatistic")
    public void afreshStatistic(Integer planId){
        CompletableFuture.runAsync(()->{
            boolean deleteByPlanId = screeningResultStatisticService.deleteByPlanId(planId);
            if (deleteByPlanId){
                statisticScheduledTaskService.statistic(null,planId,Boolean.FALSE,null,null);
            }
        },asyncServiceExecutor);
    }

    /**
     * 手动调用触发筛查结果统计定时任务
     */
    @GetMapping("/triggerAll")
    public void statTaskTrigger() {
        CompletableFuture.runAsync(()-> statisticScheduledTaskService.statistic(),asyncServiceExecutor);
    }
}
