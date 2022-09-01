package com.wupol.myopia.business.api.questionnaire.service.impl;

import com.wupol.myopia.base.constant.QuestionnaireUserType;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.questionnaire.service.IUserAnswerService;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.questionnaire.constant.UserQuestionRecordEnum;
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
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningTaskService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 政府部门
 *
 * @author Simple4H
 */
@Service
public class GovUserAnswerImpl implements IUserAnswerService {

    @Resource
    private UserAnswerService userAnswerService;

    @Resource
    private UserQuestionRecordService userQuestionRecordService;

    @Resource
    private QuestionnaireService questionnaireService;

    @Resource
    private CommonUserAnswerImpl commonUserAnswer;

    @Resource
    private GovDeptService govDeptService;

    @Resource
    private DistrictService districtService;

    @Resource
    private SchoolService schoolService;

    @Resource
    private ScreeningTaskService screeningTaskService;

    @Resource
    private UserAnswerProgressService userAnswerProgressService;

    @Override
    public Integer getUserType() {
        return QuestionnaireUserType.GOVERNMENT_DEPARTMENT.getType();
    }

    @Override
    public Integer saveUserQuestionRecord(Integer questionnaireId, Integer userId, Boolean isFinish, List<Integer> questionnaireIds, Long districtCode, Integer schoolId) {

        Integer questionnaireType = getQuestionnaireType(questionnaireId, districtCode, schoolId);

        ScreeningTask task = screeningTaskService.getOneByOrgId(userId);
        UserQuestionRecord userQuestionRecord = userQuestionRecordService.getUserQuestionRecord(userId, getUserType(), questionnaireId, schoolId, districtCode, Objects.nonNull(task) ? task.getId() : null);

        if (Objects.nonNull(userQuestionRecord)) {
            if (Objects.equals(userQuestionRecord.getStatus(), UserQuestionRecordEnum.FINISH.getType())) {
                throw new BusinessException("该问卷已经提交，不能修改！！！");
            }

            if (Objects.equals(isFinish, Boolean.TRUE)) {
                userQuestionRecord.setStatus(UserQuestionRecordEnum.FINISH.getType());
                userQuestionRecordService.updateById(userQuestionRecord);
                // 清空用户答案进度表
                UserAnswerProgress userAnswerProgress = userAnswerProgressService.getUserAnswerProgressService(userId, getUserType(), districtCode, schoolId, null);
                if (Objects.nonNull(userAnswerProgress)) {
                    userAnswerProgressService.removeById(userAnswerProgress);
                }
            }
            return userQuestionRecord.getId();
        }
        userQuestionRecord = new UserQuestionRecord();

        // 不存在新增记录
        if (Objects.nonNull(task)) {
            userQuestionRecord.setTaskId(task.getId());
            userQuestionRecord.setNoticeId(task.getScreeningNoticeId());
        }
        userQuestionRecord.setUserId(userId);
        userQuestionRecord.setUserType(getUserType());
        userQuestionRecord.setQuestionnaireId(questionnaireId);

        userQuestionRecord.setGovId(userId);
        userQuestionRecord.setDistrictCode(districtCode);
        userQuestionRecord.setSchoolId(schoolId);
        userQuestionRecord.setQuestionnaireType(questionnaireType);
        userQuestionRecord.setStatus(Objects.equals(isFinish, Boolean.TRUE) ? UserQuestionRecordEnum.FINISH.getType() : UserQuestionRecordEnum.PROCESSING.getType());
        userQuestionRecordService.save(userQuestionRecord);
        return userQuestionRecord.getId();
    }

    /**
     * 获取问卷类型
     */
    private Integer getQuestionnaireType(Integer questionnaireId, Long districtCode, Integer schoolId) {
        Questionnaire questionnaire = questionnaireService.getById(questionnaireId);
        Integer questionnaireType = questionnaire.getType();
        if (Objects.equals(questionnaireType, QuestionnaireTypeEnum.SCHOOL_ENVIRONMENT.getType()) && Objects.isNull(schoolId)) {
            throw new BusinessException("学校Id不能为空");
        }
        if (Objects.equals(questionnaireType, QuestionnaireTypeEnum.AREA_DISTRICT_SCHOOL.getType()) && Objects.isNull(districtCode)) {
            throw new BusinessException("区域Id不能为空");
        }
        return questionnaireType;
    }

    @Override
    public void deletedUserAnswer(Integer questionnaireId, Integer userId, List<UserAnswerDTO.QuestionDTO> questionList, Integer recordId) {
        List<Integer> questionIds = questionList.stream().map(UserAnswerDTO.QuestionDTO::getQuestionId).collect(Collectors.toList());
        List<UserAnswer> userAnswerList = userAnswerService.getByQuestionIds(questionnaireId, userId, getUserType(), questionIds, recordId);

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
        commonUserAnswer.saveUserProgress(isFinish, userId, getUserType(), requestDTO);
    }

    @Override
    public List<UserQuestionnaireResponseDTO> getUserQuestionnaire(Integer userId) {
        ScreeningTask task = screeningTaskService.getOneByOrgId(userId);
        if (Objects.isNull(task)) {
            throw new BusinessException("你没有问卷需要填写");
        }
        List<QuestionnaireTypeEnum> typeList = QuestionnaireTypeEnum.getGovQuestionnaireType();
        return commonUserAnswer.getUserQuestionnaire(typeList);
    }

    @Override
    public Boolean getUserAnswerIsFinish(Integer userId) {
        return commonUserAnswer.getUserAnswerIsFinish(getUserQuestionnaire(userId), userId, getUserType());
    }

    @Override
    public String getUserName(Integer userId) {
        GovDept govDept = govDeptService.getById(userId);
        if (Objects.isNull(govDept)) {
            throw new BusinessException("获取信息异常");
        }
        return govDept.getName();
    }

    /**
     * 问卷是否完成
     *
     * @return 是否完成
     */
    @Override
    public Boolean questionnaireIsFinish(Integer userId, Integer questionnaireId, Long districtCode, Integer schoolId) {
        getQuestionnaireType(questionnaireId, districtCode, schoolId);
        ScreeningTask task = screeningTaskService.getOneByOrgId(userId);
        UserQuestionRecord userQuestionRecord = userQuestionRecordService.getUserQuestionRecord(userId, getUserType(), questionnaireId, schoolId, districtCode, Objects.nonNull(task) ? task.getId() : null);
        if (Objects.isNull(userQuestionRecord)) {
            return false;
        }
        return Objects.equals(userQuestionRecord.getStatus(), UserQuestionRecordEnum.FINISH.getType());
    }

    @Override
    public List<District> getDistrict(Integer schoolId) {
        School school = schoolService.getById(schoolId);
        Integer districtId = school.getDistrictId();
        return districtService.getSameLevelDistrictKeepArea(districtId);
    }

    @Override
    public UserAnswerDTO getUserAnswerList(Integer questionnaireId, Integer userId, Long districtCode, Integer schoolId, Integer planId) {
        getQuestionnaireType(questionnaireId, districtCode, schoolId);
        ScreeningTask task = screeningTaskService.getOneByOrgId(userId);
        UserAnswerDTO userAnswerList = userAnswerService.getUserAnswerList(questionnaireId, userId, getUserType(), districtCode, schoolId, Objects.nonNull(task) ? task.getId() : null);
        UserAnswerProgress userAnswerProgress = userAnswerProgressService.getUserAnswerProgressService(userId, getUserType(), districtCode, schoolId, null);
        if (Objects.nonNull(userAnswerProgress)) {
            userAnswerList.setCurrentSideBar(userAnswerProgress.getCurrentSideBar());
            userAnswerList.setCurrentStep(userAnswerProgress.getCurrentStep());
            userAnswerList.setStepJson(userAnswerProgress.getStepJson());
        }
        return userAnswerList;
    }
}