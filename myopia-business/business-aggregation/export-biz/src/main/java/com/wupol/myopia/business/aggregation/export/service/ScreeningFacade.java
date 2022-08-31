package com.wupol.myopia.business.aggregation.export.service;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
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
}
