package com.wupol.myopia.business.api.questionnaire.service.impl;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
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
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
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
    private ScreeningPlanService screeningPlanService;

    @Resource
    private DistrictService districtService;

    @Resource
    private SchoolService schoolService;

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

        // 不存在新增记录
        ScreeningPlan govDept = screeningPlanService.getOneByGovDept(userId);
        Questionnaire questionnaire = questionnaireService.getById(questionnaireId);
        UserQuestionRecord userQuestionRecord = new UserQuestionRecord();
        userQuestionRecord.setUserId(userId);
        userQuestionRecord.setUserType(getUserType());
        userQuestionRecord.setQuestionnaireId(questionnaireId);
        userQuestionRecord.setPlanId(govDept.getId());
        userQuestionRecord.setStudentId(null);
        userQuestionRecord.setTaskId(govDept.getScreeningTaskId());
        userQuestionRecord.setNoticeId(govDept.getSrcScreeningNoticeId());
        userQuestionRecord.setGovId(userId);
        userQuestionRecord.setSchoolId(userId);
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
    public List<District> getDistrict(Integer userId, Integer schoolId) {
        School school = schoolService.getById(schoolId);
        District district = districtService.getById(school.getDistrictId());

        // 获取父节点
        List<District> districts = getAllDistrict(districtService.districtCodeToTree(district.getCode()), new ArrayList<>());

        Integer level = getLevel(districts, district.getCode(), 1);

        if (level <= 3) {
            // 获取同级的数据
            List<District> parentCode = districtService.getByParentCode(district.getParentCode());
            // 合并
            return getDistricts(districts, parentCode);
        }

        if (level == 4) {
            // 获取上级的数据
            List<District> parentCode = districtService.getByParentCode(districtService.getByCode(district.getParentCode()).getParentCode());
            // 合并
            return getDistricts(districts, parentCode);
        }
        return new ArrayList<>();
    }

    private List<District> getDistricts(List<District> districts, List<District> parentCode) {
        List<District> result = Lists.newArrayList(Iterables.concat(parentCode, districts)).stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(District::getId))),
                ArrayList::new));
        result.forEach(s->s.setChild(null));
        return districtService.districtListToTree(result, 100000000L);
    }


    private Integer getLevel(List<District> list, Long code, Integer level) {
        District district = list.get(0);
        if (Objects.equals(district.getCode(), code)) {
            return level;
        }
        level = level + 1;
        return getLevel(district.getChild(), code, level);
    }

    private List<District> getAllDistrict(List<District> list, List<District> result) {
        if (CollectionUtils.isEmpty(list)) {
            return result;
        }
        result.addAll(list);
        list.forEach(l -> getAllDistrict(l.getChild(), result));
        return result;
    }
}
