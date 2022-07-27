package com.wupol.myopia.business.api.questionnaire.service;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserQuestionnaireResponseDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 问卷
 *
 * @author Simple4H
 */
@Service
public class QuestionnaireBizService {

    @Resource
    private UserAnswerFactory userAnswerFactory;


    public List<UserQuestionnaireResponseDTO> getUserQuestionnaire(CurrentUser user) {
        IUserAnswerService userAnswerService = userAnswerFactory.getUserAnswerService(0);
        return userAnswerService.getUserQuestionnaire(user.getQuestionnaireUserId());
    }
}
