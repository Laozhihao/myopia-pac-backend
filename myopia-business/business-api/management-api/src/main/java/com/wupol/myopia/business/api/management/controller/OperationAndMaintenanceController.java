package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.aggregation.screening.service.StatConclusionBizService;
import com.wupol.myopia.business.api.management.schedule.ScheduledTasksExecutor;
import com.wupol.myopia.business.api.management.service.BigScreeningStatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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
    private ScheduledTasksExecutor scheduledTasksExecutor;

    /**
     * 触发大屏统计
     *
     * @throws IOException
     */
    @GetMapping("/big")
    public void statBigScreen() throws IOException {
        bigScreeningStatService.statisticBigScreen();
    }

    /**
     * 筛查结果数据转筛查数据结论和筛查结果统计（便于测试今天的筛查结果统计数据）
     *
     * @param planId 筛查计划ID, 不必填
     * @param isAll 是否全部 (true-全部,false-不是全部) 必填
     */
    @GetMapping("screeningToConclusion")
    public void screeningToConclusion(@RequestParam(required = false) Integer planId, @RequestParam Boolean isAll){
        statConclusionBizService.screeningToConclusion(planId,isAll);
        scheduledTasksExecutor.statistic(null,planId,isAll);
    }

}
