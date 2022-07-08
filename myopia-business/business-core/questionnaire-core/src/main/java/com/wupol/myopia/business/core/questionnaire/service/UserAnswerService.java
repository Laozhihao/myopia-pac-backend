package com.wupol.myopia.business.core.questionnaire.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserAnswerDTO;
import com.wupol.myopia.business.core.questionnaire.domain.mapper.UserAnswerMapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserAnswer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Simple4H
 */
@Service
public class UserAnswerService extends BaseService<UserAnswerMapper, UserAnswer> {

    private static final Integer USER_ID = 101;

    /**
     * 获取用户答案
     *
     * @param userId          用户Id
     * @param questionnaireId 问卷Id
     *
     * @return UserAnswerDTO
     */
    public UserAnswerDTO getUserAnswerList(Integer userId, Integer questionnaireId) {
        UserAnswerDTO userAnswerDTO = new UserAnswerDTO();
        userAnswerDTO.setQuestionnaireId(questionnaireId);
        List<UserAnswer> userAnswers = getByQuestionnaireId(questionnaireId, USER_ID);
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
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveUserAnswer(UserAnswerDTO requestDTO) {


        Integer questionnaireId = requestDTO.getQuestionnaireId();
        // 先删除当前问卷下的所有答案
        List<Integer> ids = getByQuestionnaireId(questionnaireId, USER_ID).stream().map(UserAnswer::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(ids)) {
            baseMapper.deleteBatchIds(ids);
        }

        List<UserAnswer> userAnswers = requestDTO.getQuestionList().stream().map(s -> {
            UserAnswer userAnswer = new UserAnswer();
            userAnswer.setUserId(USER_ID);
            userAnswer.setQuestionnaireId(questionnaireId);
            userAnswer.setQuestionId(s.getQuestionId());
            userAnswer.setAnswer(s.getAnswer());
            return userAnswer;
        }).collect(Collectors.toList());
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


}
