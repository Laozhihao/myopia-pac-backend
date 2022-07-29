package com.wupol.myopia.business.api.questionnaire.service;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserAnswerDTO;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserQuestionnaireResponseDTO;

import java.util.List;

/**
 * 用户答案
 *
 * @author Simple4H
 */
public interface IUserAnswerService {

    Integer getUserType();

    Integer saveUserQuestionRecord(Integer questionnaireId, CurrentUser user, Boolean isFinish, List<Integer> questionnaireIds);

    void deletedUserAnswer(Integer questionnaireId, Integer userId, List<UserAnswerDTO.QuestionDTO> questionList);

    void saveUserAnswer(UserAnswerDTO requestDTO, Integer userId, Integer recordId);

    void saveUserProgress(UserAnswerDTO requestDTO, Integer userId, Boolean isFinish);

    List<UserQuestionnaireResponseDTO> getUserQuestionnaire(Integer userId);

    Boolean getUserAnswerIsFinish(Integer userId);

    String getSchoolName(Integer userId);

    void hiddenQuestion(Integer questionnaireId, Integer userId, Integer recordId);
}
