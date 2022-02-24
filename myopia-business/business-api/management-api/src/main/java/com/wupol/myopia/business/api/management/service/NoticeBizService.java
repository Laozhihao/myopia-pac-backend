package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.organization.domain.model.Overview;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.OverviewService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.system.service.NoticeService;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Author wulizhou
 * @Date 2021/12/8 18:34
 */
@Service
public class NoticeBizService {

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private OverviewService overviewService;

    /**
     * 发送合作即将到期通知
     * @param beforeDay 通知提醒提前天数
     */
    public void sendCooperationWarnInfoNotice(int beforeDay) {
        Date now = new Date();
        Date cooperationEndTime = DateUtils.addDays(now, beforeDay);
        // 发送即将到期学校通知
        List<School> schools = schoolService.getByCooperationEndTime(now, cooperationEndTime);
        for (School school : schools) {
            sendNotice(school.getName(), school.getCooperationEndTime());
        }
        // 发送即将到期筛查机构通知
        List<ScreeningOrganization> orgs = screeningOrganizationService.getByCooperationEndTime(now, cooperationEndTime);
        for (ScreeningOrganization org : orgs) {
            sendNotice(org.getName(), org.getCooperationEndTime());
        }
        // 发送即将到期医院通知
        List<Hospital> hospitals = hospitalService.getByCooperationEndTime(now, cooperationEndTime);
        for (Hospital hospital : hospitals) {
            sendNotice(hospital.getName(), hospital.getCooperationEndTime());
        }
        // 发送即将到期总览机构通知
        List<Overview> overviews = overviewService.getByCooperationEndTime(now, cooperationEndTime);
        for (Overview overview : overviews) {
            sendNotice(overview.getName(), overview.getCooperationEndTime());
        }
    }

    private void sendNotice(String name, Date cooperationEndTime) {
        String content = String.format(CommonConst.COOPERATION_WARN_NOTICE, name, DateFormatUtils.format(cooperationEndTime, DateFormatUtil.FORMAT_TIME_WITHOUT_SECOND));
        noticeService.sendNoticeToAllAdmin(-1, content, content, CommonConst.NOTICE_STATION_LETTER);
    }

}
