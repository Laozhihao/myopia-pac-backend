package com.wupol.myopia.business.core.questionnaire.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.questionnaire.domain.mapper.UserQuestionRecordMapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import org.springframework.stereotype.Service;

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
     * @param userId          用户id
     * @param userType        用户类型
     * @param questionnaireId 问卷Id
     *
     * @return UserQuestionRecord
     */
    public UserQuestionRecord getUserQuestionRecord(Integer userId, Integer userType, Integer questionnaireId) {
        LambdaQueryWrapper<UserQuestionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserQuestionRecord::getUserId, userId);
        wrapper.eq(UserQuestionRecord::getUserType, userType);
        wrapper.eq(UserQuestionRecord::getQuestionnaireId, questionnaireId);
        return baseMapper.selectOne(wrapper);
    }

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


}
