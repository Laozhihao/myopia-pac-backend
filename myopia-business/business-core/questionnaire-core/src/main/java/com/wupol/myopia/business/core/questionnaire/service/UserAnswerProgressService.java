package com.wupol.myopia.business.core.questionnaire.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.questionnaire.domain.mapper.UserAnswerProgressMapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserAnswerProgress;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @Author Simple4H
 * @Date 2022-07-25
 */
@Service
public class UserAnswerProgressService extends BaseService<UserAnswerProgressMapper, UserAnswerProgress> {

    public UserAnswerProgress getUserAnswerProgressService(Integer userId, Integer userType, Long districtCode, Integer schoolId, Integer planId) {
        return getOne(new LambdaQueryWrapper<UserAnswerProgress>()
                .eq(UserAnswerProgress::getUserId, userId)
                .eq(UserAnswerProgress::getUserType, userType)
                .eq(Objects.nonNull(districtCode), UserAnswerProgress::getDistrictCode, districtCode)
                .eq(Objects.nonNull(schoolId), UserAnswerProgress::getSchoolId, schoolId)
                .eq(Objects.nonNull(planId), UserAnswerProgress::getPlanId, planId));
    }


}
