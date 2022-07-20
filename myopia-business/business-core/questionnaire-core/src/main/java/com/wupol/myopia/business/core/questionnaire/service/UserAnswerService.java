package com.wupol.myopia.business.core.questionnaire.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.domain.CurrentUser;
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
    public UserAnswerDTO getUserAnswerList(Integer questionnaireId, CurrentUser user) {
        UserAnswerDTO userAnswerDTO = new UserAnswerDTO();
        userAnswerDTO.setQuestionnaireId(questionnaireId);
        List<UserAnswer> userAnswers = getByQuestionnaireIdAndUserType(questionnaireId, user.getQuestionnaireUserId(), user.getQuestionnaireUserType());
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
    public void saveUserAnswer(UserAnswerDTO requestDTO, Integer userId, Integer userType) {

        Integer questionnaireId = requestDTO.getQuestionnaireId();
        List<UserAnswerDTO.QuestionDTO> questionList = requestDTO.getQuestionList();

        // 先简单处理（最后提交的最新，即最新提交的会覆盖）
        Map<Integer, Integer> questionMap = getByQuestionnaireIdAndUserType(questionnaireId, userId, userType)
                .stream().collect(Collectors.toMap(UserAnswer::getQuestionId, UserAnswer::getId));

        List<UserAnswer> userAnswers = convert2UserAnswer(questionList, questionnaireId, userId, questionMap, userType);
        baseMapper.batchSaveUserAnswer(userAnswers);
    }

    /**
     * 通过问卷Id、用户Id获取答案
     *
     * @param questionnaireId 问卷Id
     * @param userType        用户类型
     *
     * @return List<UserAnswer>
     */
    public List<UserAnswer> getByQuestionnaireIdAndUserType(Integer questionnaireId, Integer userId, Integer userType) {

        LambdaQueryWrapper<UserAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAnswer::getQuestionnaireId, questionnaireId)
                .eq(UserAnswer::getUserId, userId)
                .eq(UserAnswer::getUserType, userType);
        return baseMapper.selectList(wrapper);
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
                                                Integer userId, Map<Integer, Integer> questionMap, Integer userType) {
        return list.stream().map(s -> {
            UserAnswer userAnswer = new UserAnswer();
            userAnswer.setId(questionMap.getOrDefault(s.getQuestionId(), null));
            userAnswer.setUserId(userId);
            userAnswer.setQuestionnaireId(questionnaireId);
            userAnswer.setQuestionId(s.getQuestionId());
            userAnswer.setUserType(userType);
            userAnswer.setQuestionTitle(s.getTitle());
            userAnswer.setAnswer(s.getAnswer());
            return userAnswer;
        }).collect(Collectors.toList());
    }


}
