package com.wupol.myopia.business.api.questionnaire.service.impl;

import com.wupol.myopia.base.constant.QuestionnaireUserType;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.api.questionnaire.service.IUserAnswerService;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserAnswerDTO;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserAnswer;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireService;
import com.wupol.myopia.business.core.questionnaire.service.UserAnswerService;
import com.wupol.myopia.business.core.questionnaire.service.UserQuestionRecordService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
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

    @Override
    public Integer getUserType() {
        return QuestionnaireUserType.STUDENT.getType();
    }

    @Override
    public Integer saveUserQuestionRecord(Integer questionnaireId, CurrentUser user, Boolean isFinish) {

        ScreeningPlanSchoolStudent planStudent = screeningPlanSchoolStudentService.getById(user.getQuestionnaireUserId());

        UserQuestionRecord userQuestionRecord = userQuestionRecordService.getUserQuestionRecord(planStudent.getId(), getUserType(), questionnaireId);

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
        userQuestionRecord.setUserType(getUserType());
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

    @Override
    public void deletedUserAnswer(Integer questionnaireId, Integer userId,
                                  List<UserAnswerDTO.QuestionDTO> questionList) {
        List<UserAnswer> userAnswerList = userAnswerService.getByQuestionnaireIdAndUserTypeAndQuestionIds(questionnaireId,
                userId,
                getUserType(),
                questionList.stream().map(UserAnswerDTO.QuestionDTO::getQuestionId).collect(Collectors.toList()));

        if (!CollectionUtils.isEmpty(userAnswerList)) {
            userAnswerService.removeByIds(userAnswerList.stream().map(UserAnswer::getId).collect(Collectors.toList()));
        }
    }

    @Override
    public void saveUserAnswer(UserAnswerDTO requestDTO, Integer userId, Integer recordId) {
        userAnswerService.saveUserAnswer(requestDTO, userId, getUserType(), recordId);
    }
}
