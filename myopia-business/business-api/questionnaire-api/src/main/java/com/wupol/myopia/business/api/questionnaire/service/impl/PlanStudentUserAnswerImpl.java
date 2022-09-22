package com.wupol.myopia.business.api.questionnaire.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.constant.QuestionnaireUserType;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.questionnaire.service.IUserAnswerService;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import com.wupol.myopia.business.core.questionnaire.constant.UserQuestionRecordEnum;
import com.wupol.myopia.business.core.questionnaire.domain.dos.Option;
import com.wupol.myopia.business.core.questionnaire.domain.dos.OptionAnswer;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserAnswerDTO;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserQuestionnaireResponseDTO;
import com.wupol.myopia.business.core.questionnaire.domain.model.*;
import com.wupol.myopia.business.core.questionnaire.service.*;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    private QuestionnaireQuestionService questionnaireQuestionService;

    @Resource
    private QuestionService questionService;

    @Resource
    private CommonUserAnswerImpl commonUserAnswer;

    @Resource
    private UserAnswerProgressService userAnswerProgressService;

    @Override
    public Integer getUserType() {
        return QuestionnaireUserType.STUDENT.getType();
    }

    @Override
    public Integer saveUserQuestionRecord(Integer questionnaireId, Integer userId, Boolean isFinish, List<Integer> questionnaireIds, Long districtCode, Integer schoolId) {

        ScreeningPlanSchoolStudent planStudent = screeningPlanSchoolStudentService.getById(userId);
        if (Objects.isNull(planStudent)) {
            throw new BusinessException("学生数据异常！");
        }
        // 如果存在记录，且完成问卷，则更新状态
        Integer recordId = commonUserAnswer.finishQuestionnaire(questionnaireId, isFinish, questionnaireIds, userId, getUserType(), planStudent.getScreeningPlanId());
        if (Objects.nonNull(recordId)) {
            return recordId;
        }

        // 不存在新增记录
        Questionnaire questionnaire = questionnaireService.getById(questionnaireId);
        UserQuestionRecord userQuestionRecord = new UserQuestionRecord();
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

    /**
     * <p>如果存在多份表格的话，会存在问题<br/>
     * 前端目前是将questionId默认为-1，如果一份问卷存在多份表格，就需要区分开<br/>
     * 如：表格1-questionId为-1，表格2-questionId为-2
     * </p>
     *
     * @param questionnaireId 问卷ID
     * @param userId          用户Id
     * @param questionList    问题列表
     * @param recordId        记录表Id
     */
    @Override
    public void deletedUserAnswer(Integer questionnaireId, Integer userId, List<UserAnswerDTO.QuestionDTO> questionList, Integer recordId) {
        commonUserAnswer.deletedUserAnswer(questionList, questionnaireId, userId, getUserType());
    }

    @Override
    public void saveUserAnswer(UserAnswerDTO requestDTO, Integer userId, Integer recordId) {
        userAnswerService.saveUserAnswer(requestDTO, userId, getUserType(), recordId);
    }

    @Override
    public void saveUserProgress(UserAnswerDTO requestDTO, Integer userId, Boolean isFinish) {
        commonUserAnswer.saveUserProgress(isFinish, userId, getUserType(), requestDTO);
    }

    @Override
    public List<UserQuestionnaireResponseDTO> getUserQuestionnaire(Integer userId) {

        ScreeningPlanSchoolStudent planStudent = screeningPlanSchoolStudentService.getById(userId);
        if (Objects.isNull(planStudent)) {
            throw new BusinessException("获取信息异常");
        }

        List<QuestionnaireTypeEnum> typeList = QuestionnaireTypeEnum.getStudentQuestionnaireBySchoolAge(planStudent.getGradeType());
        return commonUserAnswer.getUserQuestionnaire(typeList);
    }

    @Override
    public Boolean getUserAnswerIsFinish(Integer userId) {
        return commonUserAnswer.getUserAnswerIsFinish(getUserQuestionnaire(userId), userId, getUserType());
    }

    @Override
    public String getUserName(Integer userId) {
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
        List<QuestionnaireQuestion> questionnaireQuestions = questionnaireQuestionService.getBySerialNumbers(questionnaireId, Lists.newArrayList(CommonConst.A01, CommonConst.A011, CommonConst.A02));
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

    @Override
    public UserAnswerDTO getUserAnswerList(Integer questionnaireId, Integer userId, Long districtCode, Integer schoolId, Integer planId) {
        UserAnswerDTO userAnswerList = userAnswerService.getUserAnswerList(questionnaireId, userId, getUserType());
        UserAnswerProgress userAnswerProgress = userAnswerProgressService.findOne(
                new UserAnswerProgress()
                        .setUserId(userId)
                        .setUserType(getUserType()));
        if (Objects.nonNull(userAnswerProgress)) {
            userAnswerList.setCurrentSideBar(userAnswerProgress.getCurrentSideBar());
            userAnswerList.setCurrentStep(userAnswerProgress.getCurrentStep());
            userAnswerList.setStepJson(userAnswerProgress.getStepJson());
        }
        return userAnswerList;
    }

    private void specialHandleAnswer(ScreeningPlanSchoolStudent planStudent, String v, UserAnswer userAnswer, List<Option> options) {
        if (StringUtils.equals(v, CommonConst.A01)) {
            OptionAnswer optionAnswer = new OptionAnswer();
            JSONObject json = JSON.parseObject(JSON.toJSONString(options.get(0).getOption().get("1")), JSONObject.class);
            optionAnswer.setOptionId(json.getString(QuestionnaireConstant.ID));
            optionAnswer.setValue(GradeCodeEnum.getByName(planStudent.getGradeName()).getCode());
            userAnswer.setAnswer(Lists.newArrayList(optionAnswer));
            userAnswerService.save(userAnswer);
            return;
        }

        if (StringUtils.equals(v, CommonConst.A011)) {
            OptionAnswer optionAnswer = new OptionAnswer();
            JSONObject json = JSON.parseObject(JSON.toJSONString(options.get(0).getOption().get("1")), JSONObject.class);
            optionAnswer.setOptionId(json.getString(QuestionnaireConstant.ID));
            if (StringUtils.isEmpty(planStudent.getCommonDiseaseId())) {
                return;
            }
            // 这里的编码可能会改变，所以为空，导出问卷的时候动态生成
            optionAnswer.setValue(StringUtils.EMPTY);
            userAnswer.setAnswer(Lists.newArrayList(optionAnswer));
            userAnswerService.save(userAnswer);
            return;
        }

        if (StringUtils.equals(v, CommonConst.A02)) {
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
        List<QuestionnaireQuestion> questionnaireQuestions = questionnaireQuestionService.getBySerialNumbers(questionnaire.getId(), CommonConst.getVisionSpineNotice());
        if (CollectionUtils.isEmpty(questionnaireQuestions)) {
            return;
        }
        List<Integer> questionIds = questionnaireQuestions.stream().map(QuestionnaireQuestion::getQuestionId).collect(Collectors.toList());
        // 获取答案
        List<UserAnswer> userAnswers = userAnswerService.getByQuestionIds(questionnaireId, userId, userType, questionIds, recordId);
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
        if (!CollectionUtils.isEmpty(userAnswerService.getByQuestionIds(questionnaire.getId(), userId, userType, questionIds, userQuestionRecord.getId()))) {
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
