package com.wupol.myopia.business.api.hospital.app.schedule;

import com.wupol.myopia.business.aggregation.hospital.service.MedicalReportBizService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


/**
 * 医院-定时任务
 * @author Chikong
 * @date 2021-05-06
 */
@Component
@Slf4j
public class HospitalScheduledTask {

    @Autowired
    private MedicalReportBizService medicalReportBizService;

    /** 生成报告的固化结论 */
    @Scheduled(cron = "0 1 0 * * *")
    public void generateReportConclusion() {
        medicalReportBizService.generateReportConclusion();
    }

}