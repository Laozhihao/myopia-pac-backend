package com.wupol.myopia.business.core.questionnaire.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.questionnaire.domain.mapper.UserQuestionRecordMapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

import java.util.Collection;
import java.util.List;

/**
 * @Author Simple4H
 * @Date 2022-07-07
 */
@Service
public class UserQuestionRecordService extends BaseService<UserQuestionRecordMapper, UserQuestionRecord> {

    /**
     * 获取用户记录表
     *
     * @param userId           用户id
     * @param userType         用户类型
     * @param questionnaireIds 问卷Ids
     *
     * @return UserQuestionRecord
     */
    public List<UserQuestionRecord> getUserQuestionRecordList(Integer userId, Integer userType, Collection<Integer> questionnaireIds) {
        LambdaQueryWrapper<UserQuestionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserQuestionRecord::getUserId, userId);
        wrapper.eq(UserQuestionRecord::getUserType, userType);
        wrapper.in(UserQuestionRecord::getQuestionnaireId, questionnaireIds);
        return baseMapper.selectList(wrapper);
    }


    /**
     * 根据计划id和type获得问卷数据
     *
     * @param userType
     * @return
     */
    public List<UserQuestionRecord> findRecordByPlanIdAndUserType(List<Integer> planIds, Integer userType) {
        if (CollectionUtils.isEmpty(planIds)) {
            return Lists.newArrayList();
        }
        return baseMapper.selectList(new LambdaQueryWrapper<UserQuestionRecord>()
                .in(UserQuestionRecord::getPlanId, planIds)
                .notIn(UserQuestionRecord::getUserType, userType)
        );
    }
}
