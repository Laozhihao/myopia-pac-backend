package com.wupol.myopia.business.core.questionnaire.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserAnswer;
import com.wupol.myopia.business.core.questionnaire.domain.mapper.UserAnswerMapper;
import com.wupol.myopia.base.service.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author Simple4H
 * @Date 2022-07-06
 */
@Service
public class UserAnswerService extends BaseService<UserAnswerMapper, UserAnswer> {

    /**
     * 根据记录ID集合 批量查询用户答案
     * @param recordIds 记录ID集合
     */
    public List<UserAnswer> getListByRecordIds(List<Integer> recordIds){
        LambdaQueryWrapper<UserAnswer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(UserAnswer::getRecordId,recordIds);
        return baseMapper.selectList(queryWrapper);
    }

}
