package com.wupol.myopia.business.api.questionnaire.service;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.questionnaire.domain.dos.Option;
import com.wupol.myopia.business.core.questionnaire.domain.dos.OptionAnswer;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserAnswerDTO;
import com.wupol.myopia.business.core.questionnaire.domain.model.*;
import com.wupol.myopia.business.core.questionnaire.service.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
    private QuestionnaireService questionnaireService;

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Resource
    private QuestionnaireQuestionService questionnaireQuestionService;

    @Resource
    private QuestionService questionService;

    @Resource
    private UserAnswerFactory userAnswerFactory;

    @Resource
    private UserAnswerProgressService userAnswerProgressService;

    @Resource
    private UserQuestionRecordService userQuestionRecordService;

    public UserAnswerDTO getUserAnswerList(Integer questionnaireId, CurrentUser user) {
        UserAnswerDTO userAnswerList = userAnswerService.getUserAnswerList(questionnaireId, user);
        UserAnswerProgress userAnswerProgress = userAnswerProgressService.findOne(
                new UserAnswerProgress()
                        .setUserId(user.getExQuestionnaireUserId())
                        .setUserType(user.getQuestionnaireUserType()));
        if (Objects.nonNull(userAnswerProgress)) {
            userAnswerList.setCurrentSideBar(userAnswerProgress.getCurrentSideBar());
            userAnswerList.setCurrentStep(userAnswerProgress.getCurrentStep());
        }
        return userAnswerList;
    }

    /**
     * 保存答案
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveUserAnswer(UserAnswerDTO requestDTO, CurrentUser user) {
        Integer questionnaireId = requestDTO.getQuestionnaireId();
        List<UserAnswerDTO.QuestionDTO> questionList = requestDTO.getQuestionList();
        Integer userId = user.getExQuestionnaireUserId();
        Integer questionnaireUserType = user.getQuestionnaireUserType();

        IUserAnswerService iUserAnswerService = userAnswerFactory.getUserAnswerService(questionnaireUserType);
        // 更新记录表
        Integer recordId = iUserAnswerService.saveUserQuestionRecord(questionnaireId, user, requestDTO.getIsFinish(), requestDTO.getQuestionnaireIds());

        // 处理隐藏问题
        hiddenQuestion(questionnaireId, userId, questionnaireUserType, recordId);

        // 先删除，后新增
        iUserAnswerService.deletedUserAnswer(questionnaireId, userId, questionList);

        // 保存用户答案
        iUserAnswerService.saveUserAnswer(requestDTO, userId, recordId);

        // 保存进度
        iUserAnswerService.saveUserProgress(requestDTO, userId, requestDTO.getIsFinish());

        // 获取用户答题状态
        return iUserAnswerService.getUserAnswerIsFinish(userId);
    }

    /**
     * 是否完成问卷
     *
     * @param user 用户
     *
     * @return Boolean
     */
    public Boolean userAnswerIsFinish(CurrentUser user) {
        IUserAnswerService iUserAnswerService = userAnswerFactory.getUserAnswerService(user.getQuestionnaireUserType());
        return iUserAnswerService.getUserAnswerIsFinish(user.getExQuestionnaireUserId());
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
        if (CollectionUtils.isEmpty(questionnaireQuestions)) {
            return;
        }

        // 是否已经存在答案
        List<Integer> questionIds = questionnaireQuestions.stream().map(QuestionnaireQuestion::getQuestionId).collect(Collectors.toList());
        List<UserAnswer> userAnswers = userAnswerService.getByQuestionIds(questionnaireId, userId, questionnaireUserType, questionIds);
        if (!CollectionUtils.isEmpty(userAnswers)) {
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
                    specialHandleAnswer(planStudent, v, userAnswer, question.getOptions());
                });

        // 处理脊柱弯曲学生基本信息
        abc(planStudent, questionnaireId, userId, questionnaireUserType, recordId);
    }

    private void specialHandleAnswer(ScreeningPlanSchoolStudent planStudent, String v, UserAnswer userAnswer, List<Option> options) {
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
            if (StringUtils.isEmpty(planStudent.getCommonDiseaseId())) {
                return;
            }
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
    }

    /**
     * 获取学校名称
     *
     * @param user 用户
     *
     * @return 学校名称
     */
    public String getSchoolName(CurrentUser user) {
        IUserAnswerService iUserAnswerService = userAnswerFactory.getUserAnswerService(user.getQuestionnaireUserType());
        return iUserAnswerService.getSchoolName(user.getExQuestionnaireUserId());
    }

    private void abc(ScreeningPlanSchoolStudent planStudent, Integer questionnaireId, Integer userId, Integer userType, Integer recordId) {
        // 需要插入到脊柱问卷的编号
        List<QuestionnaireQuestion> questionnaireQuestions = questionnaireQuestionService.getBySerialNumbers(questionnaireId, Lists.newArrayList("A01", "A011", "A02", "A04"));
        if (CollectionUtils.isEmpty(questionnaireQuestions)) {
            return;
        }
        List<Integer> questionIds = questionnaireQuestions.stream().map(QuestionnaireQuestion::getQuestionId).collect(Collectors.toList());
        // 获取答案
        List<UserAnswer> userAnswers = userAnswerService.getByQuestionIds(questionnaireId, userId, userType, recordId, questionIds);
        if (CollectionUtils.isEmpty(userAnswers)) {
            return;
        }

        // 获取最新问卷
        Questionnaire questionnaire = questionnaireService.getByType(QuestionnaireTypeEnum.VISION_SPINE_NOTICE.getType());
        if (Objects.isNull(questionnaire)) {
            return;
        }

        // 新增record表
        UserQuestionRecord userQuestionRecord = userQuestionRecordService.findOne(
                new UserQuestionRecord()
                        .setUserId(planStudent.getId())
                        .setUserType(userType)
                        .setQuestionnaireId(questionnaire.getId()));
        if (Objects.isNull(userQuestionRecord)) {
            userQuestionRecord = new UserQuestionRecord();
            userQuestionRecord.setUserId(userId);
            userQuestionRecord.setUserType(userType);
            userQuestionRecord.setQuestionnaireId(questionnaireId);
            userQuestionRecord.setPlanId(planStudent.getScreeningPlanId());
            userQuestionRecord.setTaskId(planStudent.getScreeningTaskId());
            userQuestionRecord.setNoticeId(planStudent.getSrcScreeningNoticeId());
            userQuestionRecord.setSchoolId(planStudent.getSchoolId());
            userQuestionRecord.setStudentId(planStudent.getStudentId());
            userQuestionRecord.setQuestionnaireType(QuestionnaireTypeEnum.VISION_SPINE_NOTICE.getType());
            userQuestionRecord.setStatus(1);
            userQuestionRecordService.save(userQuestionRecord);
        }

        // 问题是否已经存在
        if (!CollectionUtils.isEmpty(userAnswerService.getByQuestionIds(questionnaire.getId(), userId, userType, userQuestionRecord.getId(), questionIds))) {
            return;
        }

        for (UserAnswer userAnswer : userAnswers) {
            userAnswer.setId(null);
            userAnswer.setQuestionnaireId(questionnaire.getId());
            userAnswer.setRecordId(userQuestionRecord.getId());
        }
        userAnswerService.saveBatch(userAnswers);
    }

}
