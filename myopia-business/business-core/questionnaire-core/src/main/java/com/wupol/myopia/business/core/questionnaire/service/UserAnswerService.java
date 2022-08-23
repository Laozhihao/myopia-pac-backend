package com.wupol.myopia.business.core.questionnaire.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.questionnaire.domain.dos.OptionAnswer;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QesDataDO;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserAnswerDTO;
import com.wupol.myopia.business.core.questionnaire.domain.mapper.UserAnswerMapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQuestion;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserAnswer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Simple4H
 */
@Service
public class UserAnswerService extends BaseService<UserAnswerMapper, UserAnswer> {

    @Resource
    private QuestionnaireQuestionService questionnaireQuestionService;

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
        List<UserAnswer> userAnswers = getByQuestionnaireIdAndUserType(questionnaireId, user.getExQuestionnaireUserId(), user.getQuestionnaireUserType());
        userAnswerDTO.setQuestionList(userAnswers.stream().map(s -> {
            UserAnswerDTO.QuestionDTO questionDTO = new UserAnswerDTO.QuestionDTO();
            questionDTO.setQuestionId(s.getQuestionId());
            questionDTO.setAnswer(s.getAnswer());
            questionDTO.setTableJson(s.getTableJson());
            questionDTO.setType(s.getType());
            questionDTO.setMappingKey(s.getMappingKey());
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
    public void saveUserAnswer(UserAnswerDTO requestDTO, Integer userId, Integer userType, Integer recordId) {

        Integer questionnaireId = requestDTO.getQuestionnaireId();
        List<UserAnswerDTO.QuestionDTO> questionList = requestDTO.getQuestionList();

        handleQesData(questionnaireId, questionList);

        // 删除旧答案
        List<Integer> questionIds = questionList.stream().map(UserAnswerDTO.QuestionDTO::getQuestionId).collect(Collectors.toList());
        baseMapper.deleteBatchByCombinationId(questionnaireId, userId, userType, recordId, questionIds);

        List<UserAnswer> userAnswers = convert2UserAnswer(questionList, questionnaireId, userId, userType, recordId);
        baseMapper.batchSaveUserAnswer(userAnswers);
    }

    /**
     * 处理qes信息
     *
     * @param questionnaireId 问卷Id
     * @param questionList    问题答案列表
     */
    private void handleQesData(Integer questionnaireId, List<UserAnswerDTO.QuestionDTO> questionList) {

        List<Integer> questionIds = questionList.stream().map(UserAnswerDTO.QuestionDTO::getQuestionId).collect(Collectors.toList());
        List<QuestionnaireQuestion> questionnaireQuestions = questionnaireQuestionService.getByQuestionnaireIdAndQuestionIds(questionnaireId, questionIds);
        Map<String, QesDataDO> qesDataMap = questionnaireQuestions.stream()
                .map(QuestionnaireQuestion::getQesData).flatMap(Collection::stream)
                .collect(Collectors.toMap(QesDataDO::getOptionId, Function.identity()));

        List<OptionAnswer> answerList = questionList.stream().map(UserAnswerDTO.QuestionDTO::getAnswer).filter(Objects::nonNull)
                .flatMap(Collection::stream).collect(Collectors.toList());

        answerList.forEach(answer -> {
            QesDataDO qesDataDOS = qesDataMap.get(answer.getOptionId());
            if (Objects.nonNull(qesDataDOS)) {
                answer.setQesField(qesDataDOS.getQesField());
                answer.setShowSerialNumber(qesDataDOS.getShowSerialNumber());
                answer.setQesSerialNumber(qesDataDOS.getQesSerialNumber());
                answer.setDataType(answer.getDataType());
            }
        });
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
     *
     * @return List<UserAnswer>
     */
    private List<UserAnswer> convert2UserAnswer(List<UserAnswerDTO.QuestionDTO> list, Integer questionnaireId,
                                                Integer userId, Integer userType, Integer recordId) {
        return list.stream().map(s -> {
            UserAnswer userAnswer = new UserAnswer();
            userAnswer.setUserId(userId);
            userAnswer.setQuestionnaireId(questionnaireId);
            Integer questionId = s.getQuestionId();
            if (Objects.isNull(questionId)) {
                throw new BusinessException("问题Id异常!");
            }
            userAnswer.setQuestionId(questionId);
            userAnswer.setRecordId(recordId);
            userAnswer.setUserType(userType);
            userAnswer.setQuestionTitle(s.getTitle());
            userAnswer.setAnswer(s.getAnswer());
            userAnswer.setTableJson(s.getTableJson());
            userAnswer.setType(s.getType());
            userAnswer.setMappingKey(s.getMappingKey());
            return userAnswer;
        }).collect(Collectors.toList());
    }

    /**
     * 通过问题Id获取答案
     *
     * @return List<UserAnswer>
     */
    public List<UserAnswer> getByQuestionIds(Integer questionnaireId, Integer userId, Integer userType, Collection<Integer> questionIds) {

        LambdaQueryWrapper<UserAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAnswer::getQuestionnaireId, questionnaireId)
                .eq(UserAnswer::getUserId, userId)
                .eq(UserAnswer::getUserType, userType)
                .in(UserAnswer::getQuestionId, questionIds);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 通过问题Id获取答案
     *
     * @return List<UserAnswer>
     */
    public List<UserAnswer> getByQuestionIds(Integer questionnaireId, Integer userId, Integer userType, Integer recordId, Collection<Integer> questionIds) {

        LambdaQueryWrapper<UserAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAnswer::getQuestionnaireId, questionnaireId)
                .eq(UserAnswer::getUserId, userId)
                .eq(UserAnswer::getUserType, userType)
                .in(UserAnswer::getQuestionId, questionIds)
                .eq(UserAnswer::getRecordId, recordId);
        return baseMapper.selectList(wrapper);
    }


    /**
     * 根据记录ID集合 批量查询用户答案
     *
     * @param recordIds 记录ID集合
     */
    public List<UserAnswer> getListByRecordIds(List<Integer> recordIds) {
        LambdaQueryWrapper<UserAnswer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(UserAnswer::getRecordId, recordIds);
        return baseMapper.selectList(queryWrapper);
    }

}
