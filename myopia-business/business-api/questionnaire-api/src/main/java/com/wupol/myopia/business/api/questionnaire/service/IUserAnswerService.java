package com.wupol.myopia.business.api.questionnaire.service;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserAnswerDTO;

import java.util.List;

/**
 * 用户答案
 *
 * @author Simple4H
 */
public interface IUserAnswerService {

    Integer getUserType();

    Integer saveUserQuestionRecord(Integer questionnaireId, CurrentUser user, Boolean isFinish);

    void deletedUserAnswer(Integer questionnaireId, Integer userId, List<UserAnswerDTO.QuestionDTO> questionList);

    void saveUserAnswer(UserAnswerDTO requestDTO, Integer userId, Integer recordId);

    void saveUserProgress(UserAnswerDTO requestDTO, Integer userId);
}
