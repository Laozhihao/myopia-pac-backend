package com.wupol.myopia.business.core.questionnaire.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserAnswerDTO;
import com.wupol.myopia.business.core.questionnaire.domain.mapper.UserAnswerMapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserAnswer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Simple4H
 */
@Service
public class UserAnswerService extends BaseService<UserAnswerMapper, UserAnswer> {

    /**
     * 获取用户答案
     *
     * @param questionnaireId 问卷Id
     *
     * @return UserAnswerDTO
     */
    public UserAnswerDTO getUserAnswerList(Integer questionnaireId, Integer userId) {
        UserAnswerDTO userAnswerDTO = new UserAnswerDTO();
        userAnswerDTO.setQuestionnaireId(questionnaireId);
        List<UserAnswer> userAnswers = getByQuestionnaireId(questionnaireId, userId);
        userAnswerDTO.setQuestionList(userAnswers.stream().map(s -> {
            UserAnswerDTO.QuestionDTO questionDTO = new UserAnswerDTO.QuestionDTO();
            questionDTO.setQuestionId(s.getQuestionId());
            questionDTO.setAnswer(s.getAnswer());
            return questionDTO;
        }).collect(Collectors.toList()));
        return userAnswerDTO;
    }

    /**
     * 保存用户答案
     *
     * @param requestDTO 请求DTO
     * @param userId     用户Id
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveUserAnswer(UserAnswerDTO requestDTO, Integer userId) {

        Integer questionnaireId = requestDTO.getQuestionnaireId();
        List<UserAnswerDTO.QuestionDTO> questionList = requestDTO.getQuestionList();

        // 先简单处理（最后提交的最新，即最新提交的会覆盖）
        Map<Integer, Integer> questionMap = getByQuestionnaireId(questionnaireId, userId)
                .stream().collect(Collectors.toMap(UserAnswer::getQuestionId, UserAnswer::getId));

        List<UserAnswer> userAnswers = convert2UserAnswer(questionList, questionnaireId, userId, questionMap);
        baseMapper.batchSaveUserAnswer(userAnswers);
    }

    /**
     * 通过问卷Id、用户Id获取答案
     *
     * @param questionnaireId 问卷Id
     *
     * @return List<UserAnswer>
     */
    public List<UserAnswer> getByQuestionnaireId(Integer questionnaireId, Integer userId) {
        return baseMapper.getByQuestionnaireId(questionnaireId, userId);
    }

    /**
     * 转换成UserAnswer
     *
     * @param list            请求参数
     * @param questionnaireId 问卷Id
     * @param questionMap     存在的答案Map
     *
     * @return List<UserAnswer>
     */
    private List<UserAnswer> convert2UserAnswer(List<UserAnswerDTO.QuestionDTO> list, Integer questionnaireId,
                                                Integer userId, Map<Integer, Integer> questionMap) {
        return list.stream().map(s -> {
            UserAnswer userAnswer = new UserAnswer();
            userAnswer.setId(questionMap.getOrDefault(s.getQuestionId(), null));
            userAnswer.setUserId(userId);
            userAnswer.setQuestionnaireId(questionnaireId);
            userAnswer.setQuestionId(s.getQuestionId());
            userAnswer.setQuestionTitle(s.getTitle());
            userAnswer.setAnswer(s.getAnswer());
            return userAnswer;
        }).collect(Collectors.toList());
    }


}
