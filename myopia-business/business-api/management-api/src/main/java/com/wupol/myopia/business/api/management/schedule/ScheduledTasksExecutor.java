package com.wupol.myopia.business.api.management.schedule;

import cn.hutool.core.collection.CollUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.api.management.service.*;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Alix
 * @date 2021/02/19
 */
@Component
@Slf4j
public class ScheduledTasksExecutor {

    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private StudentBizService studentBizService;
    @Autowired
    private StatService statService;
    @Autowired
    private NoticeBizService noticeBizService;
    @Autowired
    private CooperationService cooperationService;
    @Autowired
    private PreSchoolNoticeService preSchoolNoticeService;
    @Autowired
    private StatisticScheduledTaskService statisticScheduledTaskService;
    @Autowired
    private NoticeLinkService noticeLinkService;

    /**
     * 筛查数据统计
     */
    @Scheduled(cron = "0 5 1 * * ?")
    public void statisticScreeningDataDaily() {
        statisticScheduledTaskService.statisticScreeningData();
    }

    /**
     * 每天9点执行，发送短信
     */
    // @Scheduled(cron = "0 0 9 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void sendSMSNotice() {
        List<VisionScreeningResult> studentResult = visionScreeningResultService.getStudentResults();
        if (CollUtil.isEmpty(studentResult)) {
            return;
        }
        // 获取学生信息
        List<Integer> studentIds = studentResult.stream()
                .map(VisionScreeningResult::getStudentId).collect(Collectors.toList());
        List<Student> students = studentService.getByIds(studentIds);
        Map<Integer, Student> studentMaps = students.stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));

        studentResult.forEach(studentBizService.getVisionScreeningResultConsumer(studentMaps));
        visionScreeningResultService.updateBatchById(studentResult);
    }

    /**
     * 每天23点30分执行，复测统计
     */
    @Scheduled(cron = "0 30 23 * * ?")
    public void rescreenStat() {
        Date screeningTime = DateUtils.addDays(DateUtil.getMidday(new Date()), -1);
        log.info("开始进行复测报告统计,筛查时间为:{}", screeningTime);
        int size = statService.rescreenStat(screeningTime);
        log.info("本次复测统计共新增加内容{}条", size);
    }

    /**
     * 合作状态处理：包含机构、医院、学校、总览机构<br/>
     * 每5分执行
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void cooperationStatusHandle() {
        log.debug("开始进行机构（筛查机构、学校、医院、总览机构）状态更新");
        Date date = new Date();
        log.debug("本次任务共更新筛查机构状态{}条", cooperationService.handleOrganizationStatus(date));
        log.debug("本次任务共更新学校状态{}条", cooperationService.handleSchoolStatus(date));
        log.debug("本次任务共更新医院状态{}条", cooperationService.handleHospitalStatus(date));
        log.debug("本次任务共更新总览机构状态{}条", cooperationService.handleOverviewStatus(date));
    }

    /**
     * 合作即将到期通知<br/>
     * 每日10点执行
     */
    @Scheduled(cron = "0 0 10 * * ?")
    public void cooperationWarnInfoNotice() {
        log.debug("开始进行合作机构（筛查机构、学校、医院）即将到期通知");
        // 提前7天通知
        noticeBizService.sendCooperationWarnInfoNotice(7);
    }

    /**
     * 孩子年龄到了后会短信或公众号提醒家长做保健
     * 每日10点执行
     */
    @Scheduled(cron = "0 0 10 * * ?")
    public void preschoolCheckNotice() {
        // 提前7天通知
        preSchoolNoticeService.timedTaskSendMsg();
    }

    /**
     * 关联通知
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void abc() {
        log.info("开始关联任务");
        noticeLinkService.migratingStudentData();
    }

}