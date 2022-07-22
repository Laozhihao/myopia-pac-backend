package com.wupol.myopia.business.api.questionnaire.service;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.questionnaire.domain.dto.Option;
import com.wupol.myopia.business.core.questionnaire.domain.dto.OptionAnswer;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserAnswerDTO;
import com.wupol.myopia.business.core.questionnaire.domain.model.*;
import com.wupol.myopia.business.core.questionnaire.service.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
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
    private UserQuestionRecordService userQuestionRecordService;

    @Resource
    private QuestionnaireService questionnaireService;

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Resource
    private QuestionnaireQuestionService questionnaireQuestionService;

    @Resource
    private QuestionService questionService;

    /**
     * 保存答案
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveUserAnswer(UserAnswerDTO requestDTO, CurrentUser user) {
        Integer questionnaireId = requestDTO.getQuestionnaireId();
        List<UserAnswerDTO.QuestionDTO> questionList = requestDTO.getQuestionList();
        Integer userId = user.getQuestionnaireUserId();
        Integer questionnaireUserType = user.getQuestionnaireUserType();

        // 更新记录表
        Integer recordId = saveUserQuestionRecord(questionnaireId, user, requestDTO.getIsFinish());

        // 处理隐藏问题
        hiddenQuestion(questionnaireId, userId, questionnaireUserType, recordId);

        // 先简单处理，先删除，后新增
        List<UserAnswer> userAnswerList = userAnswerService.getByQuestionnaireIdAndUserTypeAndQuestionIds(questionnaireId,
                userId,
                questionnaireUserType,
                questionList.stream().map(UserAnswerDTO.QuestionDTO::getQuestionId).collect(Collectors.toList()));

        if (CollectionUtil.isNotEmpty(userAnswerList)) {
            userAnswerService.removeByIds(userAnswerList.stream().map(UserAnswer::getId).collect(Collectors.toList()));
        }

        // 保存用户答案
        userAnswerService.saveUserAnswer(requestDTO, userId, questionnaireUserType, recordId);
    }

    /**
     * 更新记录表
     */
    private Integer saveUserQuestionRecord(Integer questionnaireId, CurrentUser user, Boolean isFinish) {
        if (user.isQuestionnaireStudentUser()) {
            Integer questionnaireUserType = user.getQuestionnaireUserType();
            ScreeningPlanSchoolStudent planStudent = screeningPlanSchoolStudentService.getById(user.getQuestionnaireUserId());

            UserQuestionRecord userQuestionRecord = userQuestionRecordService.getUserQuestionRecord(planStudent.getId(), questionnaireUserType, questionnaireId);

            // 如果存在记录，且完成问卷，则更新状态
            if (Objects.nonNull(userQuestionRecord)) {
                if (Objects.equals(isFinish, Boolean.TRUE)) {
                    userQuestionRecord.setStatus(2);
                    userQuestionRecordService.updateById(userQuestionRecord);
                }
                return userQuestionRecord.getId();
            }

            // 不存在新增记录
            Questionnaire questionnaire = questionnaireService.getById(questionnaireId);
            userQuestionRecord = new UserQuestionRecord();
            userQuestionRecord.setUserId(planStudent.getId());
            userQuestionRecord.setUserType(questionnaireUserType);
            userQuestionRecord.setQuestionnaireId(questionnaireId);
            userQuestionRecord.setPlanId(planStudent.getScreeningPlanId());
            userQuestionRecord.setTaskId(planStudent.getScreeningTaskId());
            userQuestionRecord.setNoticeId(planStudent.getSrcScreeningNoticeId());
            userQuestionRecord.setSchoolId(planStudent.getSchoolId());
            userQuestionRecord.setQuestionnaireType(questionnaire.getType());
            userQuestionRecord.setStudentId(planStudent.getStudentId());
            userQuestionRecord.setStatus(1);
            userQuestionRecordService.save(userQuestionRecord);
            return userQuestionRecord.getId();
        }
        return null;
    }

    /**
     * 隐藏问题
     */
    private void hiddenQuestion(Integer questionnaireId, Integer userId, Integer questionnaireUserType, Integer recordId) {
        Questionnaire questionnaire = questionnaireService.getById(questionnaireId);
        if (!Objects.equals(questionnaire.getType(), QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE.getType())) {
            return;
        }

        // 获取问卷中是否存在序号为A01，A011，A02三个问题
        List<QuestionnaireQuestion> questionnaireQuestions = questionnaireQuestionService.getBySerialNumbers(questionnaireId, Lists.newArrayList("A01", "A011", "A02"));
        if (CollectionUtil.isEmpty(questionnaireQuestions)) {
            return;
        }

        // 是否已经存在答案
        List<Integer> questionIds = questionnaireQuestions.stream().map(QuestionnaireQuestion::getQuestionId).collect(Collectors.toList());
        List<UserAnswer> userAnswers = userAnswerService.getByQuestionIds(questionnaireId, userId, questionnaireUserType, questionIds);
        if (CollectionUtil.isNotEmpty(userAnswers)) {
            return;
        }

        // 不存在则添加答案
        ScreeningPlanSchoolStudent planStudent = screeningPlanSchoolStudentService.getById(userId);
        Map<Integer, Question> questionMap = questionService.listByIds(questionIds).stream().collect(Collectors.toMap(Question::getId, Function.identity()));

        questionnaireQuestions.stream()
                .collect(Collectors.toMap(QuestionnaireQuestion::getQuestionId, QuestionnaireQuestion::getSerialNumber))
                .forEach((k, v) -> {
                    Question question = questionMap.get(k);
                    UserAnswer userAnswer = new UserAnswer();
                    userAnswer.setUserId(userId);
                    userAnswer.setQuestionnaireId(questionnaireId);
                    userAnswer.setQuestionId(question.getId());
                    userAnswer.setRecordId(recordId);
                    userAnswer.setUserType(questionnaireUserType);
                    userAnswer.setQuestionTitle(question.getTitle());
                    List<Option> options = JSONObject.parseArray(JSONObject.toJSONString(question.getOptions()), Option.class);

                    if (StringUtils.equals(v, "A01")) {
                        OptionAnswer optionAnswer = new OptionAnswer();
                        optionAnswer.setOptionId(options.get(0).getId());
                        optionAnswer.setValue(planStudent.getGradeName());
                        userAnswer.setAnswer(Lists.newArrayList(optionAnswer));
                        userAnswerService.save(userAnswer);
                        return;
                    }

                    if (StringUtils.equals(v, "A011")) {
                        OptionAnswer optionAnswer = new OptionAnswer();
                        optionAnswer.setOptionId(options.get(0).getId());
                        optionAnswer.setValue(planStudent.getCommonDiseaseId().substring(planStudent.getCommonDiseaseId().length() - 4));
                        userAnswer.setAnswer(Lists.newArrayList(optionAnswer));
                        userAnswerService.save(userAnswer);
                        return;
                    }

                    if (StringUtils.equals(v, "A02")) {
                        Optional<Option> optionOptional = options.stream().filter(s -> StringUtils.equals(s.getText(), GenderEnum.getName(planStudent.getGender()))).findFirst();
                        if (optionOptional.isPresent()) {
                            OptionAnswer optionAnswer = new OptionAnswer();
                            optionAnswer.setOptionId(optionOptional.get().getId());
                            userAnswer.setAnswer(Lists.newArrayList(optionAnswer));
                            userAnswerService.save(userAnswer);
                        }

                    }
                });
    }

}
