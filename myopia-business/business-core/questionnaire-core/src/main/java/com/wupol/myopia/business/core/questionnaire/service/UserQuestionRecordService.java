package com.wupol.myopia.business.core.questionnaire.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.questionnaire.domain.mapper.UserQuestionRecordMapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
        wrapper.orderByDesc(UserQuestionRecord::getCreateTime);
        return baseMapper.selectList(wrapper);
    }


    /**
     * 根据计划id和type获得问卷数据
     *
     * @param userType
     *
     * @return
     */
    public List<UserQuestionRecord> findRecordByPlanIdAndUserType(List<Integer> planIds, Integer userType,Integer status) {
        if (CollectionUtils.isEmpty(planIds)) {
            return Lists.newArrayList();
        }
        return baseMapper.selectList(new LambdaQueryWrapper<UserQuestionRecord>()
                .in(UserQuestionRecord::getPlanId, planIds)
                .eq(UserQuestionRecord::getUserType, userType)
                .eq(UserQuestionRecord::getStatus,status)
        );
    }

    /**
     * 根据筛查计划ID获取用户答问卷记录集合
     *
     * @param planId 计划ID
     * @param status 状态
     */
    public List<UserQuestionRecord> getListByPlanId(Integer planId, Integer status) {
        return getListByNoticeIdOrTaskIdOrPlanId(null, null, planId, status);
    }

    /**
     * 根据筛查通知ID获取用户答问卷记录集合
     *
     * @param noticeId 通知ID
     * @param status   状态
     */
    public List<UserQuestionRecord> getListByNoticeId(Integer noticeId, Integer status) {
        return getListByNoticeIdOrTaskIdOrPlanId(noticeId, null, null, status);
    }

    /**
     * 根据筛查任务ID获取用户答问卷记录集合
     *
     * @param taskId 任务ID
     * @param status 状态
     */
    public List<UserQuestionRecord> getListByTaskId(Integer taskId, Integer status) {
        return getListByNoticeIdOrTaskIdOrPlanId(null, taskId, null, status);
    }

    /**
     * 根据筛查通知ID或筛查任务ID或筛查计划ID 和状态 获取用户答问卷记录集合
     *
     * @param noticeId 通知ID
     * @param taskId   任务ID
     * @param planId   计划ID
     * @param status   状态
     */
    public List<UserQuestionRecord> getListByNoticeIdOrTaskIdOrPlanId(Integer noticeId, Integer taskId, Integer planId, Integer status) {
        LambdaQueryWrapper<UserQuestionRecord> queryWrapper = new LambdaQueryWrapper<>();
        Optional.ofNullable(noticeId).ifPresent(id -> queryWrapper.eq(UserQuestionRecord::getNoticeId, noticeId));
        Optional.ofNullable(taskId).ifPresent(id -> queryWrapper.eq(UserQuestionRecord::getTaskId, taskId));
        Optional.ofNullable(planId).ifPresent(id -> queryWrapper.eq(UserQuestionRecord::getPlanId, planId));
        if (queryWrapper.isEmptyOfWhere()) {
            return Lists.newArrayList();
        }
        Optional.ofNullable(status).ifPresent(s -> queryWrapper.eq(UserQuestionRecord::getStatus, status));
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 根据筛查ID集合批量获取用户答问卷记录集合
     *
     * @param planIds 筛查ID集合
     */
    public List<UserQuestionRecord> getListByPlanIds(List<Integer> planIds) {
        LambdaQueryWrapper<UserQuestionRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(UserQuestionRecord::getPlanId, planIds);
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 获取用户记录表
     *
     * @param userId          用户id
     * @param userType        用户类型
     * @param questionnaireId 问卷Id
     *
     * @return UserQuestionRecord
     */
    public UserQuestionRecord getUserQuestionRecord(Integer userId, Integer userType, Integer questionnaireId, Integer planId) {
        return findOne(new UserQuestionRecord()
                .setUserId(userId)
                .setUserType(userType)
                .setQuestionnaireId(questionnaireId)
                .setPlanId(planId));
    }

    /**
     * 获取用户记录表
     *
     * @return UserQuestionRecord
     */
    public UserQuestionRecord getUserQuestionRecord(Integer userId, Integer userType, Integer questionnaireId, Integer schoolId, Long districtCode, Integer taskId) {
        return getOne(new LambdaQueryWrapper<UserQuestionRecord>()
                .eq(UserQuestionRecord::getUserId, userId)
                .eq(UserQuestionRecord::getUserType, userType)
                .eq(UserQuestionRecord::getQuestionnaireId, questionnaireId)
                .eq(Objects.nonNull(schoolId), UserQuestionRecord::getSchoolId, schoolId)
                .eq(Objects.nonNull(districtCode), UserQuestionRecord::getDistrictCode, districtCode)
                .eq(Objects.nonNull(taskId), UserQuestionRecord::getTaskId, taskId));
    }


    /**
     * 根据任务Id和问卷类型查询
     * @param taskId
     * @param questionnaireType
     * @param status
     */
    public List<UserQuestionRecord> listByTaskIdAndType(Integer taskId,Integer questionnaireType ,Integer status){
        LambdaQueryWrapper<UserQuestionRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserQuestionRecord::getTaskId,taskId);
        queryWrapper.eq(UserQuestionRecord::getQuestionnaireType,questionnaireType);
        queryWrapper.eq(UserQuestionRecord::getStatus,status);
        return baseMapper.selectList(queryWrapper);
    }

}
