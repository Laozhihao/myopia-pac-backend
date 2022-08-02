package com.wupol.myopia.business.api.questionnaire.service.impl;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.constant.QuestionnaireUserType;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.questionnaire.service.IUserAnswerService;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireMainTitleEnum;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.questionnaire.constant.UserQuestionRecordEnum;
import com.wupol.myopia.business.core.questionnaire.domain.dos.Option;
import com.wupol.myopia.business.core.questionnaire.domain.dos.OptionAnswer;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserAnswerDTO;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserQuestionnaireResponseDTO;
import com.wupol.myopia.business.core.questionnaire.domain.model.*;
import com.wupol.myopia.business.core.questionnaire.service.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 学生
 *
 * @author Simple4H
 */
@Service
public class PlanStudentUserAnswerImpl implements IUserAnswerService {

    @Resource
    private UserAnswerService userAnswerService;

    @Resource
    private UserQuestionRecordService userQuestionRecordService;

    @Resource
    private QuestionnaireService questionnaireService;

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Resource
    private UserAnswerProgressService userAnswerProgressService;

    @Resource
    private QuestionnaireQuestionService questionnaireQuestionService;

    @Resource
    private QuestionService questionService;

    @Override
    public Integer getUserType() {
        return QuestionnaireUserType.STUDENT.getType();
    }

    @Override
    public Integer saveUserQuestionRecord(Integer questionnaireId, Integer userId, Boolean isFinish, List<Integer> questionnaireIds) {

        ScreeningPlanSchoolStudent planStudent = screeningPlanSchoolStudentService.getById(userId);
        UserQuestionRecord userQuestionRecord = userQuestionRecordService.findOne(
                new UserQuestionRecord()
                        .setUserId(planStudent.getId())
                        .setUserType(getUserType())
                        .setQuestionnaireId(questionnaireId));

        // 如果存在记录，且完成问卷，则更新状态
        if (Objects.nonNull(userQuestionRecord)) {
            if (Objects.equals(isFinish, Boolean.TRUE)) {
                List<UserQuestionRecord> userQuestionRecordList = userQuestionRecordService.getUserQuestionRecordList(planStudent.getId(), getUserType(), questionnaireIds);
                userQuestionRecordList.forEach(item -> item.setStatus(UserQuestionRecordEnum.FINISH.getType()));
                userQuestionRecordService.updateBatchById(userQuestionRecordList);
                // 清空用户答案进度表
                UserAnswerProgress userAnswerProgress = userAnswerProgressService.findOne(
                        new UserAnswerProgress()
                                .setUserId(userId)
                                .setUserType(getUserType()));
                if (Objects.nonNull(userAnswerProgress)) {
                    userAnswerProgress.setCurrentStep(null);
                    userAnswerProgress.setCurrentSideBar(null);
                    userAnswerProgress.setUpdateTime(new Date());
                    userAnswerProgressService.updateById(userAnswerProgress);
                }
            }
            return userQuestionRecord.getId();
        }

        // 不存在新增记录
        Questionnaire questionnaire = questionnaireService.getById(questionnaireId);
        userQuestionRecord = new UserQuestionRecord();
        userQuestionRecord.setUserId(planStudent.getId());
        userQuestionRecord.setUserType(getUserType());
        userQuestionRecord.setQuestionnaireId(questionnaireId);
        userQuestionRecord.setPlanId(planStudent.getScreeningPlanId());
        userQuestionRecord.setTaskId(planStudent.getScreeningTaskId());
        userQuestionRecord.setNoticeId(planStudent.getSrcScreeningNoticeId());
        userQuestionRecord.setSchoolId(planStudent.getSchoolId());
        userQuestionRecord.setQuestionnaireType(questionnaire.getType());
        userQuestionRecord.setStudentId(planStudent.getStudentId());
        userQuestionRecord.setStatus(UserQuestionRecordEnum.PROCESSING.getType());
        userQuestionRecordService.save(userQuestionRecord);
        return userQuestionRecord.getId();
    }

    @Override
    public void deletedUserAnswer(Integer questionnaireId, Integer userId, List<UserAnswerDTO.QuestionDTO> questionList) {
        List<UserAnswer> userAnswerList = userAnswerService.getByQuestionIds(questionnaireId, userId, getUserType(), questionList.stream().map(UserAnswerDTO.QuestionDTO::getQuestionId).collect(Collectors.toList()));

        if (!CollectionUtils.isEmpty(userAnswerList)) {
            userAnswerService.removeByIds(userAnswerList.stream().map(UserAnswer::getId).collect(Collectors.toList()));
        }
    }

    @Override
    public void saveUserAnswer(UserAnswerDTO requestDTO, Integer userId, Integer recordId) {
        userAnswerService.saveUserAnswer(requestDTO, userId, getUserType(), recordId);
    }

    @Override
    public void saveUserProgress(UserAnswerDTO requestDTO, Integer userId, Boolean isFinish) {
        // 完成不需要保存进度
        if (Objects.equals(isFinish, Boolean.TRUE)) {
            return;
        }
        UserAnswerProgress userAnswerProgress = userAnswerProgressService.findOne(
                new UserAnswerProgress()
                        .setUserId(userId)
                        .setUserType(getUserType()));

        if (Objects.isNull(userAnswerProgress)) {
            userAnswerProgress = new UserAnswerProgress();
            userAnswerProgress.setUserId(userId);
            userAnswerProgress.setUserType(getUserType());
        }
        userAnswerProgress.setCurrentStep(requestDTO.getCurrentStep());
        userAnswerProgress.setCurrentSideBar(requestDTO.getCurrentSideBar());
        userAnswerProgressService.saveOrUpdate(userAnswerProgress);
    }

    @Override
    public List<UserQuestionnaireResponseDTO> getUserQuestionnaire(Integer userId) {

        ScreeningPlanSchoolStudent planStudent = screeningPlanSchoolStudentService.getById(userId);
        if (Objects.isNull(planStudent)) {
            throw new BusinessException("获取信息异常");
        }

        List<QuestionnaireTypeEnum> typeList = QuestionnaireTypeEnum.getBySchoolAge(planStudent.getGradeType());
        if (CollectionUtils.isEmpty(typeList)) {
            return new ArrayList<>();
        }

        // 获取问卷
        Map<Integer, Questionnaire> typeMap = questionnaireService.getByTypes(typeList.stream().map(QuestionnaireTypeEnum::getType).collect(Collectors.toList())).stream().collect(Collectors.toMap(Questionnaire::getType, Function.identity()));

        return typeList.stream().map(s -> {
            UserQuestionnaireResponseDTO responseDTO = new UserQuestionnaireResponseDTO();
            Questionnaire questionnaire = typeMap.get(s.getType());
            responseDTO.setId(questionnaire.getId());
            responseDTO.setTitle(s.getDesc());
            responseDTO.setMainTitle(QuestionnaireMainTitleEnum.getByType(s.getType()).getMainTitle());
            return responseDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public Boolean getUserAnswerIsFinish(Integer userId) {
        List<UserQuestionnaireResponseDTO> userQuestionnaire = getUserQuestionnaire(userId);
        List<Integer> questionnaireIds = userQuestionnaire.stream().map(UserQuestionnaireResponseDTO::getId).collect(Collectors.toList());
        List<UserQuestionRecord> userQuestionRecordList = userQuestionRecordService.getUserQuestionRecordList(userId, getUserType(), questionnaireIds);
        if (CollectionUtils.isEmpty(userQuestionRecordList)) {
            return false;
        }
        // 多份问卷，状态是统一的
        return Objects.equals(userQuestionRecordList.get(0).getStatus(), UserQuestionRecordEnum.FINISH.getType());
    }

    @Override
    public String getSchoolName(Integer userId) {
        ScreeningPlanSchoolStudent planStudent = screeningPlanSchoolStudentService.getById(userId);
        if (Objects.isNull(planStudent)) {
            throw new BusinessException("获取信息异常");
        }
        return planStudent.getSchoolName();
    }

    @Override
    public void hiddenQuestion(Integer questionnaireId, Integer userId, Integer recordId) {
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
        List<UserAnswer> userAnswers = userAnswerService.getByQuestionIds(questionnaireId, userId, getUserType(), questionIds);
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
                    userAnswer.setUserType(getUserType());
                    userAnswer.setQuestionTitle(question.getTitle());
                    specialHandleAnswer(planStudent, v, userAnswer, question.getOptions());
                });

        // 处理脊柱弯曲学生基本信息
        addVisionSpineNotice(planStudent, questionnaireId, userId, getUserType(), recordId);
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
            // 这里的编码可能会改变，所以为空，导出问卷的时候动态生成
            optionAnswer.setValue(StringUtils.EMPTY);
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

    private void addVisionSpineNotice(ScreeningPlanSchoolStudent planStudent, Integer questionnaireId, Integer userId, Integer userType, Integer recordId) {

        // 获取最新问卷
        Questionnaire questionnaire = questionnaireService.getByType(QuestionnaireTypeEnum.VISION_SPINE_NOTICE.getType());
        if (Objects.isNull(questionnaire)) {
            return;
        }

        // 需要插入到脊柱问卷的编号
        List<QuestionnaireQuestion> questionnaireQuestions = questionnaireQuestionService.getBySerialNumbers(questionnaire.getId(), CommonConst.VISION_SPINE_NOTICE);
        if (CollectionUtils.isEmpty(questionnaireQuestions)) {
            return;
        }
        List<Integer> questionIds = questionnaireQuestions.stream().map(QuestionnaireQuestion::getQuestionId).collect(Collectors.toList());
        // 获取答案
        List<UserAnswer> userAnswers = userAnswerService.getByQuestionIds(questionnaireId, userId, userType, recordId, questionIds);
        if (CollectionUtils.isEmpty(userAnswers)) {
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
            userQuestionRecord.setQuestionnaireId(questionnaire.getId());
            userQuestionRecord.setPlanId(planStudent.getScreeningPlanId());
            userQuestionRecord.setTaskId(planStudent.getScreeningTaskId());
            userQuestionRecord.setNoticeId(planStudent.getSrcScreeningNoticeId());
            userQuestionRecord.setSchoolId(planStudent.getSchoolId());
            userQuestionRecord.setStudentId(planStudent.getStudentId());
            userQuestionRecord.setQuestionnaireType(QuestionnaireTypeEnum.VISION_SPINE_NOTICE.getType());
            userQuestionRecord.setStatus(UserQuestionRecordEnum.PROCESSING.getType());
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
