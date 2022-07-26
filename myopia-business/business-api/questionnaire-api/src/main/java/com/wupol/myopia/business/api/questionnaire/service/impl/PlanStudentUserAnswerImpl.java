package com.wupol.myopia.business.api.questionnaire.service.impl;

import com.wupol.myopia.base.constant.QuestionnaireUserType;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.questionnaire.service.IUserAnswerService;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireMainTitleEnum;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserAnswerDTO;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserQuestionnaireResponseDTO;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserAnswer;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserAnswerProgress;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireService;
import com.wupol.myopia.business.core.questionnaire.service.UserAnswerProgressService;
import com.wupol.myopia.business.core.questionnaire.service.UserAnswerService;
import com.wupol.myopia.business.core.questionnaire.service.UserQuestionRecordService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
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

    @Override
    public Integer getUserType() {
        return QuestionnaireUserType.STUDENT.getType();
    }

    @Override
    public Integer saveUserQuestionRecord(Integer questionnaireId, CurrentUser user, Boolean isFinish, List<Integer> questionnaireIds) {

        Integer userId = user.getExQuestionnaireUserId();
        ScreeningPlanSchoolStudent planStudent = screeningPlanSchoolStudentService.getById(userId);
        UserQuestionRecord userQuestionRecord = userQuestionRecordService.getUserQuestionRecord(planStudent.getId(), getUserType(), questionnaireId);

        // 如果存在记录，且完成问卷，则更新状态
        if (Objects.nonNull(userQuestionRecord)) {
            if (Objects.equals(isFinish, Boolean.TRUE)) {
                List<UserQuestionRecord> userQuestionRecordList = userQuestionRecordService.getUserQuestionRecordList(planStudent.getId(), getUserType(), questionnaireIds);
                userQuestionRecordList.forEach(item -> item.setStatus(2));
                userQuestionRecordService.updateBatchById(userQuestionRecordList);
                // 清空用户答案进度表
                UserAnswerProgress userAnswerProgress = userAnswerProgressService.getUserAnswerProgress(userId, getUserType());
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
        userQuestionRecord.setStatus(1);
        userQuestionRecordService.save(userQuestionRecord);
        return userQuestionRecord.getId();
    }

    @Override
    public void deletedUserAnswer(Integer questionnaireId, Integer userId,
                                  List<UserAnswerDTO.QuestionDTO> questionList) {
        List<UserAnswer> userAnswerList = userAnswerService.getByQuestionIds(questionnaireId,
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

    @Override
    public void saveUserProgress(UserAnswerDTO requestDTO, Integer userId, Boolean isFinish) {
        // 完成不需要保存进度
        if (Objects.equals(isFinish, Boolean.TRUE)) {
            return;
        }
        UserAnswerProgress userAnswerProgress = userAnswerProgressService.getUserAnswerProgress(userId, getUserType());

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
        Map<Integer, Questionnaire> typeMap = questionnaireService.getByTypes(
                        typeList.stream().map(QuestionnaireTypeEnum::getType).collect(Collectors.toList())).stream()
                .collect(Collectors.toMap(Questionnaire::getType, Function.identity()));

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
        return Objects.equals(userQuestionRecordList.get(0).getStatus(), 2);
    }


}
