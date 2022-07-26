package com.wupol.myopia.business.api.questionnaire.service.impl;

import com.wupol.myopia.base.constant.QuestionnaireUserType;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.api.questionnaire.service.IUserAnswerService;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserAnswerDTO;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserQuestionnaireResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 学校
 *
 * @author Simple4H
 */
@Service
public class SchoolUserAnswerImpl implements IUserAnswerService {

    @Override
    public Integer getUserType() {
        return QuestionnaireUserType.SCHOOL.getType();
    }

    @Override
    public Integer saveUserQuestionRecord(Integer questionnaireId, CurrentUser user, Boolean isFinish, List<Integer> questionnaireIds) {
        return null;
    }

    @Override
    public void deletedUserAnswer(Integer questionnaireId, Integer userId, List<UserAnswerDTO.QuestionDTO> questionList) {

    }

    @Override
    public void saveUserAnswer(UserAnswerDTO requestDTO, Integer userId, Integer recordId) {

    }

    @Override
    public void saveUserProgress(UserAnswerDTO requestDTO, Integer userId, Boolean isFinish) {

    }

    @Override
    public List<UserQuestionnaireResponseDTO> getUserQuestionnaire(Integer userId) {
        return null;
    }

    @Override
    public Boolean getUserAnswerIsFinish(Integer userId) {
        return null;
    }
}
