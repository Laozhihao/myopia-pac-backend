package com.wupol.myopia.business.core.questionnaire.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.questionnaire.domain.mapper.UserQuestionRecordMapper;
import com.wupol.myopia.base.service.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author Simple4H
 * @Date 2022-07-07
 */
@Service
public class UserQuestionRecordService extends BaseService<UserQuestionRecordMapper, UserQuestionRecord> {
    /**
     * 根据计划id和type获得问卷数据
     *
     * @param planId
     * @param typesIds
     * @return
     */
    public List<UserQuestionRecord> findRecordByPlanIdAndTypeNotIn(List<Integer> planId, List<Integer> typesIds) {
        //TODO：根据planId和userType获取学生问卷记录
        return baseMapper.selectList(new LambdaQueryWrapper<UserQuestionRecord>()
                .in(UserQuestionRecord::getPlanId, planId)
                .notIn(UserQuestionRecord::getQuestionnaireType, typesIds)
        );
    }
}
