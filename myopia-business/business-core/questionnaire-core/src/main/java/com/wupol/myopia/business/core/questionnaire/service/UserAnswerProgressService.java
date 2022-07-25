package com.wupol.myopia.business.core.questionnaire.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.questionnaire.domain.mapper.UserAnswerProgressMapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserAnswerProgress;
import org.springframework.stereotype.Service;

/**
 * @Author Simple4H
 * @Date 2022-07-25
 */
@Service
public class UserAnswerProgressService extends BaseService<UserAnswerProgressMapper, UserAnswerProgress> {

    public UserAnswerProgress getUserAnswerProgress(Integer userId, Integer userType) {
        LambdaQueryWrapper<UserAnswerProgress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAnswerProgress::getUserId, userId)
                .eq(UserAnswerProgress::getUserType, userType);
        return baseMapper.selectOne(wrapper);
    }

}
