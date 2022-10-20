package com.wupol.myopia.business.aggregation.export.service;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.model.ScreeningConfig;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningOrgTypeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 筛查门面
 *
 * @author hang.yuan 2022/8/30 12:19
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ScreeningFacade {

    private final ScreeningPlanService screeningPlanService;
    private final ScreeningOrganizationService screeningOrganizationService;
    private final SchoolService schoolService;

    /**
     * 过滤作废筛查计划数据
     * @param userQuestionRecordList 用户问卷记录集合
     */
    public List<UserQuestionRecord> filterByPlanId(List<UserQuestionRecord> userQuestionRecordList){
        if (CollUtil.isEmpty(userQuestionRecordList)){
            return userQuestionRecordList;
        }
        //筛查过滤作废的
        Set<Integer> noticeIds = userQuestionRecordList.stream().map(UserQuestionRecord::getNoticeId).collect(Collectors.toSet());
        List<ScreeningPlan> screeningPlanList = screeningPlanService.getPlanByNoticeIdsAndStatusBatch(Lists.newArrayList(noticeIds), CommonConst.STATUS_RELEASE);
        if (CollUtil.isNotEmpty(screeningPlanList)){
            Set<Integer> planIds = screeningPlanList.stream().map(ScreeningPlan::getId).collect(Collectors.toSet());
            userQuestionRecordList = userQuestionRecordList.stream()
                    .filter(userQuestionRecord -> planIds.contains(userQuestionRecord.getPlanId()) || Objects.isNull(userQuestionRecord.getPlanId()) )
                    .collect(Collectors.toList());
        }
        return userQuestionRecordList;
    }

    /**
     * 获取筛查是否是海南版
     * @param plan
     * @param screeningOrgType
     */
    public Boolean getIsHaiNan(ScreeningPlan plan, Integer screeningOrgType) {
        boolean isHaiNan = false;
        if (Objects.equals(screeningOrgType, ScreeningOrgTypeEnum.ORG.getType())){
            ScreeningOrganization screeningOrganization = screeningOrganizationService.getById(plan.getScreeningOrgId());
            isHaiNan = Objects.equals(Optional.ofNullable(screeningOrganization).map(ScreeningOrganization::getScreeningConfig).map(ScreeningConfig::getChannel).orElse(null), CommonConst.HAI_NAN);
        }else if (Objects.equals(screeningOrgType, ScreeningOrgTypeEnum.SCHOOL.getType())){
            School school = schoolService.getById(plan.getScreeningOrgId());
            isHaiNan = Objects.equals(Optional.ofNullable(school).map(School::getScreeningConfig).map(ScreeningConfig::getChannel).orElse(null), CommonConst.HAI_NAN);
        }
        return isHaiNan;
    }
}
