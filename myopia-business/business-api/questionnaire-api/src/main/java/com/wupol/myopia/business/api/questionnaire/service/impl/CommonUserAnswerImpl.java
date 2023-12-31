package com.wupol.myopia.business.api.questionnaire.service.impl;

import com.wupol.myopia.base.constant.QuestionnaireUserType;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireMainTitleEnum;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
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
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 保存答案公共类
 *
 * <p>
 * 待优化：<br/>
 * 此处的公共类可以采用抽象类（这次不调整可以看看如何抽象）<br/>
 * 结构描述：一个总接口,一个抽象类（用于实现公共方法）实现总接口,学校、政府、学生实现类继承抽象类
 * </p>
 *
 * @author Simple4H
 */
@Service
public class CommonUserAnswerImpl {

    @Resource
    private UserAnswerService userAnswerService;

    @Resource
    private UserQuestionRecordService userQuestionRecordService;

    @Resource
    private QuestionnaireService questionnaireService;

    @Resource
    private UserAnswerProgressService userAnswerProgressService;

    /**
     * 是否完成问卷
     *
     * @return 记录表Id
     */
    public Integer finishQuestionnaire(Integer questionnaireId, Boolean isFinish, List<Integer> questionnaireIds, Integer userId, Integer userType, Integer planId) {
        UserQuestionRecord userQuestionRecord = userQuestionRecordService.getUserQuestionRecord(userId, userType, questionnaireId, planId);

        if (Objects.isNull(userQuestionRecord)) {
            return null;
        }

        if (Objects.equals(userQuestionRecord.getStatus(), UserQuestionRecordEnum.FINISH.getType())) {
            throw new BusinessException("该问卷已经提交，不能修改！！！");
        }

        if (Objects.equals(isFinish, Boolean.FALSE)) {
            return userQuestionRecord.getId();
        }

        Questionnaire questionnaire = questionnaireService.getByType(QuestionnaireTypeEnum.VISION_SPINE_NOTICE.getType());
        if (Objects.nonNull(questionnaire)) {
            questionnaireIds.add(questionnaire.getId());
        }
        List<UserQuestionRecord> userQuestionRecordList = userQuestionRecordService.getUserQuestionRecordList(userId, userType, questionnaireIds);
        userQuestionRecordList.forEach(item -> item.setStatus(UserQuestionRecordEnum.FINISH.getType()));
        userQuestionRecordService.updateBatchById(userQuestionRecordList);
        // 清空用户答案进度表
        UserAnswerProgress userAnswerProgress = userAnswerProgressService.getUserAnswerProgressService(userId, userType, null, null, planId);
        if (Objects.nonNull(userAnswerProgress)) {
            userAnswerProgressService.removeById(userAnswerProgress);
        }
        // 学生新增汇总信息
        if (Objects.equals(userType, QuestionnaireUserType.STUDENT.getType())) {
            // 新增一条汇总的数据
            UserQuestionRecord totalRecord = userQuestionRecordList.get(0);
            totalRecord.setId(null);
            totalRecord.setQuestionnaireId(-1);
            totalRecord.setQuestionnaireType(null);
            totalRecord.setRecordType(1);
            totalRecord.setCreateTime(new Date());
            totalRecord.setUpdateTime(new Date());
            userQuestionRecordService.save(totalRecord);
        }
        return userQuestionRecord.getId();
    }

    /**
     * 删除用户答案
     */
    public void deletedUserAnswer(List<UserAnswerDTO.QuestionDTO> questionList, Integer questionnaireId, Integer userId, Integer userType) {
        List<Integer> questionIds = questionList.stream().map(UserAnswerDTO.QuestionDTO::getQuestionId).collect(Collectors.toList());
        List<UserAnswer> userAnswerList = userAnswerService.getByQuestionIds(questionnaireId, userId, userType, questionIds);

        if (!CollectionUtils.isEmpty(userAnswerList)) {
            userAnswerService.removeByIds(userAnswerList.stream().map(UserAnswer::getId).collect(Collectors.toList()));
        }
    }

    /**
     * 保存进度
     */
    public void saveUserProgress(Boolean isFinish, Integer userId, Integer userType, UserAnswerDTO requestDTO) {
        // 完成不需要保存进度
        if (Objects.equals(isFinish, Boolean.TRUE)) {
            return;
        }
        UserAnswerProgress userAnswerProgress = userAnswerProgressService.getUserAnswerProgressService(userId, userType, requestDTO.getDistrictCode(), requestDTO.getSchoolId(), requestDTO.getPlanId());

        if (Objects.isNull(userAnswerProgress)) {
            userAnswerProgress = new UserAnswerProgress();
            userAnswerProgress.setUserId(userId);
            userAnswerProgress.setUserType(userType);
        }
        userAnswerProgress.setCurrentStep(requestDTO.getCurrentStep());
        userAnswerProgress.setCurrentSideBar(requestDTO.getCurrentSideBar());
        userAnswerProgress.setStepJson(requestDTO.getStepJson());
        userAnswerProgress.setDistrictCode(requestDTO.getDistrictCode());
        userAnswerProgress.setSchoolId(requestDTO.getSchoolId());
        userAnswerProgress.setPlanId(requestDTO.getPlanId());
        userAnswerProgressService.saveOrUpdate(userAnswerProgress);
    }

    /**
     * 用户是否已经完成
     *
     * @return 是否已经完成
     */
    public Boolean getUserAnswerIsFinish(List<UserQuestionnaireResponseDTO> userQuestionnaire, Integer userId, Integer userType) {
        List<Integer> questionnaireIds = userQuestionnaire.stream().map(UserQuestionnaireResponseDTO::getId).collect(Collectors.toList());
        List<UserQuestionRecord> userQuestionRecordList = userQuestionRecordService.getUserQuestionRecordList(userId, userType, questionnaireIds);
        if (CollectionUtils.isEmpty(userQuestionRecordList)) {
            return false;
        }
        // 多份问卷，状态是统一的
        return Objects.equals(userQuestionRecordList.get(0).getStatus(), UserQuestionRecordEnum.FINISH.getType());
    }

    /**
     * 获取问卷列表
     *
     * @return 问卷列表
     */
    public List<UserQuestionnaireResponseDTO> getUserQuestionnaire(List<QuestionnaireTypeEnum> typeList) {
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

    /**
     * 问卷是否完成
     *
     * @return 是否完成
     */
    public Boolean questionnaireIsFinish(Integer userId, Integer userType, Integer questionnaireId, Integer planId) {
        UserQuestionRecord userQuestionRecord = userQuestionRecordService.getUserQuestionRecord(userId, userType, questionnaireId, planId);
        if (Objects.isNull(userQuestionRecord)) {
            return false;
        }
        return Objects.equals(userQuestionRecord.getStatus(), UserQuestionRecordEnum.FINISH.getType());
    }
}
