package com.wupol.myopia.business.api.questionnaire.service.impl;

import com.wupol.myopia.base.constant.QuestionnaireUserType;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.questionnaire.service.IUserAnswerService;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.core.questionnaire.constant.UserQuestionRecordEnum;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserAnswerDTO;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserQuestionnaireResponseDTO;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserAnswerProgress;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireService;
import com.wupol.myopia.business.core.questionnaire.service.UserAnswerProgressService;
import com.wupol.myopia.business.core.questionnaire.service.UserAnswerService;
import com.wupol.myopia.business.core.questionnaire.service.UserQuestionRecordService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 学校
 *
 * @author Simple4H
 */
@Service
public class SchoolUserAnswerImpl implements IUserAnswerService {

    @Resource
    private UserAnswerService userAnswerService;

    @Resource
    private UserQuestionRecordService userQuestionRecordService;

    @Resource
    private QuestionnaireService questionnaireService;

    @Resource
    private CommonUserAnswerImpl commonUserAnswer;

    @Resource
    private ScreeningPlanSchoolService screeningPlanSchoolService;

    @Resource
    private ScreeningPlanService screeningPlanService;

    @Resource
    private UserAnswerProgressService userAnswerProgressService;

    @Override
    public Integer getUserType() {
        return QuestionnaireUserType.SCHOOL.getType();
    }

    @Override
    public Integer saveUserQuestionRecord(Integer questionnaireId, Integer userId, Boolean isFinish, List<Integer> questionnaireIds, Long districtCode, Integer schoolId) {

        ScreeningPlanSchool screeningPlanSchool = screeningPlanSchoolService.getLastBySchoolIdAndScreeningType(userId, ScreeningTypeEnum.COMMON_DISEASE.getType());
        // 如果存在记录，且完成问卷，则更新状态
        Integer recordId = commonUserAnswer.finishQuestionnaire(questionnaireId, isFinish, questionnaireIds, userId, getUserType(), screeningPlanSchool.getScreeningPlanId());
        if (Objects.nonNull(recordId)) {
            return recordId;
        }

        // 不存在新增记录
        Integer screeningPlanId = screeningPlanSchool.getScreeningPlanId();
        ScreeningPlan plan = screeningPlanService.getById(screeningPlanId);

        Questionnaire questionnaire = questionnaireService.getById(questionnaireId);
        UserQuestionRecord userQuestionRecord = new UserQuestionRecord();
        userQuestionRecord.setUserId(userId);
        userQuestionRecord.setUserType(getUserType());
        userQuestionRecord.setQuestionnaireId(questionnaireId);

        userQuestionRecord.setPlanId(screeningPlanId);
        userQuestionRecord.setTaskId(plan.getScreeningTaskId());
        userQuestionRecord.setNoticeId(plan.getSrcScreeningNoticeId());
        userQuestionRecord.setSchoolId(userId);
        userQuestionRecord.setQuestionnaireType(questionnaire.getType());
        userQuestionRecord.setStudentId(null);
        userQuestionRecord.setStatus(UserQuestionRecordEnum.PROCESSING.getType());
        userQuestionRecordService.save(userQuestionRecord);
        return userQuestionRecord.getId();
    }

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
        List<QuestionnaireTypeEnum> typeList = QuestionnaireTypeEnum.getSchoolQuestionnaireType();
        return commonUserAnswer.getUserQuestionnaire(typeList);
    }

    @Override
    public Boolean getUserAnswerIsFinish(Integer userId) {
        return commonUserAnswer.getUserAnswerIsFinish(getUserQuestionnaire(userId), userId, getUserType());
    }

    @Override
    public String getUserName(Integer userId) {
        ScreeningPlanSchool screeningPlanSchool = screeningPlanSchoolService.getLastBySchoolIdAndScreeningType(userId, ScreeningTypeEnum.COMMON_DISEASE.getType());
        if (Objects.isNull(screeningPlanSchool)) {
            throw new BusinessException("获取信息异常");
        }
        return screeningPlanSchool.getSchoolName();
    }

    /**
     * 问卷是否完成
     *
     * @return 是否完成
     */
    @Override
    public Boolean questionnaireIsFinish(Integer userId, Integer questionnaireId, Long districtCode, Integer schoolId) {
        ScreeningPlanSchool screeningPlanSchool = screeningPlanSchoolService.getLastBySchoolIdAndScreeningType(userId, ScreeningTypeEnum.COMMON_DISEASE.getType());
        return commonUserAnswer.questionnaireIsFinish(userId, getUserType(), questionnaireId, screeningPlanSchool.getScreeningPlanId());
    }

    @Override
    public UserAnswerDTO getUserAnswerList(Integer questionnaireId, Integer userId, Long districtCode, Integer schoolId) {
        Integer screeningPlanId = screeningPlanSchoolService.getLastBySchoolIdAndScreeningType(userId, ScreeningTypeEnum.COMMON_DISEASE.getType()).getScreeningPlanId();
        UserQuestionRecord userQuestionRecord = userQuestionRecordService.getUserQuestionRecord(userId,
                getUserType(),
                questionnaireId,
                screeningPlanId);

        if (Objects.isNull(userQuestionRecord)) {
            return new UserAnswerDTO();
        }
        UserAnswerDTO userAnswerList = userAnswerService.getUserAnswerList(questionnaireId, userId, getUserType(), userQuestionRecord.getId());

        UserAnswerProgress userAnswerProgress = userAnswerProgressService.getUserAnswerProgressService(userId, getUserType(), null, null, screeningPlanId);
        if (Objects.nonNull(userAnswerProgress)) {
            userAnswerList.setCurrentSideBar(userAnswerProgress.getCurrentSideBar());
            userAnswerList.setCurrentStep(userAnswerProgress.getCurrentStep());
            userAnswerList.setStepJson(userAnswerProgress.getStepJson());
        }
        return userAnswerList;
    }

    @Override
    public void preCheck(UserAnswerDTO userAnswerDTO) {
        if (Objects.isNull(userAnswerDTO.getPlanId())) {
            throw new BusinessException("计划Id不能为空");
        }
    }
}
