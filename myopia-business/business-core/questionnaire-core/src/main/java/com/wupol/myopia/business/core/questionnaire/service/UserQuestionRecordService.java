package com.wupol.myopia.business.core.questionnaire.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.questionnaire.domain.mapper.UserQuestionRecordMapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
     * @param typesIds
     * @return
     */
    public List<UserQuestionRecord> findRecordByPlanIdAndTypeNotIn(List<Integer> planIds, List<Integer> typesIds) {
        //TODO：根据planId和userType获取学生问卷记录
        if (CollectionUtils.isEmpty(planIds)) {
            return Lists.newArrayList();
        }
        return baseMapper.selectList(new LambdaQueryWrapper<UserQuestionRecord>()
                .in(UserQuestionRecord::getPlanId, planIds)
                .notIn(UserQuestionRecord::getQuestionnaireType, typesIds)
        );
    }

    /**
     * 根据筛查计划ID获取用户答问卷记录集合
     * @param planId 计划ID
     */
    public List<UserQuestionRecord> getListByPlanId(Integer planId){
        return getListByNoticeIdOrTaskIdOrPlanId(null,null,planId);
    }

    /**
     * 根据筛查通知ID获取用户答问卷记录集合
     * @param noticeId 通知ID
     */
    public List<UserQuestionRecord> getListByNoticeId(Integer noticeId){
        return getListByNoticeIdOrTaskIdOrPlanId(noticeId,null,null);
    }

    /**
     * 根据筛查任务ID获取用户答问卷记录集合
     * @param taskId 任务ID
     */
    public List<UserQuestionRecord> getListByTaskId(Integer taskId){
        return getListByNoticeIdOrTaskIdOrPlanId(null,taskId,null);
    }

    /**
     * 根据筛查通知ID或筛查任务ID或筛查计划ID获取用户答问卷记录集合
     * @param noticeId 通知ID
     * @param taskId 任务ID
     * @param planId 计划ID
     */
    public List<UserQuestionRecord> getListByNoticeIdOrTaskIdOrPlanId(Integer noticeId,Integer taskId,Integer planId){
        LambdaQueryWrapper<UserQuestionRecord> queryWrapper = new LambdaQueryWrapper<>();
        Optional.ofNullable(noticeId).ifPresent(id->queryWrapper.eq(UserQuestionRecord::getNoticeId,noticeId));
        Optional.ofNullable(taskId).ifPresent(id->queryWrapper.eq(UserQuestionRecord::getTaskId,taskId));
        Optional.ofNullable(planId).ifPresent(id->queryWrapper.eq(UserQuestionRecord::getPlanId,planId));
        if (queryWrapper.isEmptyOfWhere()) {
            return Lists.newArrayList();
        }
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 根据筛查ID集合批量获取用户答问卷记录集合
     * @param planIds 筛查ID集合
     */
    public List<UserQuestionRecord> getListByPlanIds(List<Integer> planIds){
        LambdaQueryWrapper<UserQuestionRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(UserQuestionRecord::getPlanId,planIds);
        return baseMapper.selectList(queryWrapper);
    }

}
