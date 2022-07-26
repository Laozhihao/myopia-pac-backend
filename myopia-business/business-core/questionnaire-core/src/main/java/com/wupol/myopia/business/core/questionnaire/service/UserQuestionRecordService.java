package com.wupol.myopia.business.core.questionnaire.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.business.core.questionnaire.domain.dto.ExcelDataConditionBO;
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
     * 根据筛查ID获取用户答问卷记录集合
     * @param planId 筛查ID
     */
    public List<UserQuestionRecord> getListByPlanId(Integer planId){
        LambdaQueryWrapper<UserQuestionRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserQuestionRecord::getPlanId,planId);
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
