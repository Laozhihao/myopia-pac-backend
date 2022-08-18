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
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireService;
import com.wupol.myopia.business.core.questionnaire.service.UserAnswerService;
import com.wupol.myopia.business.core.questionnaire.service.UserQuestionRecordService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningTaskService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
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

    @Override
    public Integer getUserType() {
        return QuestionnaireUserType.GOVERNMENT_DEPARTMENT.getType();
    }

    @Override
    public Integer saveUserQuestionRecord(Integer questionnaireId, Integer userId, Boolean isFinish, List<Integer> questionnaireIds) {

        // 如果存在记录，且完成问卷，则更新状态
        Integer recordId = commonUserAnswer.finishQuestionnaire(questionnaireId, isFinish, questionnaireIds, userId, getUserType());
        if (Objects.nonNull(recordId)) {
            return recordId;
        }
        UserQuestionRecord userQuestionRecord = new UserQuestionRecord();

        // 不存在新增记录
        ScreeningTask task = screeningTaskService.getOneByOrgId(userId);
        if (Objects.nonNull(task)) {
//            userQuestionRecord.setPlanId();
//            userQuestionRecord.setSchoolId();
//            userQuestionRecord.setStudentId();
            userQuestionRecord.setTaskId(task.getId());
            userQuestionRecord.setNoticeId(task.getScreeningNoticeId());
        }
        Questionnaire questionnaire = questionnaireService.getById(questionnaireId);
        userQuestionRecord.setUserId(userId);
        userQuestionRecord.setUserType(getUserType());
        userQuestionRecord.setQuestionnaireId(questionnaireId);

        userQuestionRecord.setGovId(userId);
        userQuestionRecord.setQuestionnaireType(questionnaire.getType());
        userQuestionRecord.setStatus(Objects.equals(isFinish, Boolean.TRUE) ? UserQuestionRecordEnum.FINISH.getType() : UserQuestionRecordEnum.PROCESSING.getType());
        userQuestionRecordService.save(userQuestionRecord);
        return userQuestionRecord.getId();
    }

    @Override
    public void deletedUserAnswer(Integer questionnaireId, Integer userId, List<UserAnswerDTO.QuestionDTO> questionList) {
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
    public Boolean questionnaireIsFinish(Integer userId, Integer questionnaireId) {
        return commonUserAnswer.questionnaireIsFinish(userId, getUserType(), questionnaireId);
    }

    @Override
    public List<District> getDistrict(Integer schoolId) {
        School school = schoolService.getById(schoolId);
        District district = districtService.getById(school.getDistrictId());

        // 获取父节点
        List<District> districts = districtService.getAllDistrict(districtService.districtCodeToTree(district.getCode()), new ArrayList<>());
        Integer level = districtService.getLevel(districts, district.getCode(), 1);

        if (level <= 3) {
            // 获取同级的数据
            List<District> parentCode = districtService.getByParentCode(district.getParentCode());
            // 合并
            return districtService.keepAreaDistrictsTree(districts, parentCode);
        }
        if (level == 4) {
            // 获取上级的数据
            List<District> parentCode = districtService.getByParentCode(districtService.getByCode(district.getParentCode()).getParentCode());
            // 合并
            return districtService.keepAreaDistrictsTree(districts.stream().filter(s -> !Objects.equals(s.getCode(), district.getCode())).collect(Collectors.toList()), parentCode);
        }
        return new ArrayList<>();
    }
}
