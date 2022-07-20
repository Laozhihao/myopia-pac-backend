package com.wupol.myopia.business.api.questionnaire.service;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserAnswerDTO;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQuestion;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserAnswer;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireQuestionService;
import com.wupol.myopia.business.core.questionnaire.service.UserAnswerService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户答案
 *
 * @author Simple4H
 */
@Service
public class UserAnswerBizService {

    @Resource
    private UserAnswerService userAnswerService;

    @Resource
    private QuestionnaireQuestionService questionnaireQuestionService;

    /**
     * 保存答案
     */
    public void saveUserAnswer(UserAnswerDTO requestDTO, CurrentUser user) {
        Integer questionnaireId = requestDTO.getQuestionnaireId();
        Integer userId = user.getQuestionnaireUserId();
        Integer questionnaireUserType = user.getQuestionnaireUserType();
        userAnswerService.saveUserAnswer(requestDTO, userId, questionnaireUserType);

        // 如果是完成的话，简单的校验一下
//        if (requestDTO.getIsFinish()) {
//            List<QuestionnaireQuestion> questions = questionnaireQuestionService.getByQuestionnaireId(questionnaireId);
//            // 获取问卷中所有必答问题
//            List<Integer> allRequiredQuestionId = questions.stream().filter(s -> Objects.equals(s.getRequired(), Boolean.TRUE)).map(QuestionnaireQuestion::getQuestionId).collect(Collectors.toList());
//            // 获取用户所有的答案
//            List<UserAnswer> userAnswer = userAnswerService.getByQuestionnaireIdAndUserType(questionnaireId, userId, questionnaireUserType);
//            List<Integer> answerQuestionId = userAnswer.stream().map(UserAnswer::getQuestionId).collect(Collectors.toList());
//            if (answerQuestionId.size() < allRequiredQuestionId.size()) {
//                throw new BusinessException("存在必填项没有填写");
//            }
//        }

    }
}
