package com.wupol.myopia.business.api.questionnaire.service;

import cn.hutool.core.collection.CollectionUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserAnswerDTO;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserAnswer;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireQuestionService;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireService;
import com.wupol.myopia.business.core.questionnaire.service.UserAnswerService;
import com.wupol.myopia.business.core.questionnaire.service.UserQuestionRecordService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Resource
    private UserQuestionRecordService userQuestionRecordService;

    @Resource
    private QuestionnaireService questionnaireService;

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    /**
     * 保存答案
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveUserAnswer(UserAnswerDTO requestDTO, CurrentUser user) {
        Integer questionnaireId = requestDTO.getQuestionnaireId();
        List<UserAnswerDTO.QuestionDTO> questionList = requestDTO.getQuestionList();
        Integer userId = user.getQuestionnaireUserId();
        Integer questionnaireUserType = user.getQuestionnaireUserType();


        // 先简单处理，先删除，后新增
        List<UserAnswer> userAnswerList = userAnswerService.getByQuestionnaireIdAndUserTypeAndQuestionIds(questionnaireId,
                userId,
                questionnaireUserType,
                questionList.stream().map(UserAnswerDTO.QuestionDTO::getQuestionId).collect(Collectors.toList()));

        if (CollectionUtil.isNotEmpty(userAnswerList)) {
            userAnswerService.removeByIds(userAnswerList.stream().map(UserAnswer::getId).collect(Collectors.toList()));
        }

        // 更新记录表
        Integer recordId = saveUserQuestionRecord(questionnaireId, user, requestDTO.getIsFinish());

        userAnswerService.saveUserAnswer(requestDTO, userId, questionnaireUserType, recordId);

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
}
