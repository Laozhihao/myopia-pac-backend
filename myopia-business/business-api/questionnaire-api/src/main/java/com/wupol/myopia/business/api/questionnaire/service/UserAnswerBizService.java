package com.wupol.myopia.business.api.questionnaire.service;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserAnswerDTO;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserAnswerProgress;
import com.wupol.myopia.business.core.questionnaire.service.UserAnswerProgressService;
import com.wupol.myopia.business.core.questionnaire.service.UserAnswerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

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
    private UserAnswerFactory userAnswerFactory;

    @Resource
    private UserAnswerProgressService userAnswerProgressService;

    /**
     * 获取用户答案
     */
    public UserAnswerDTO getUserAnswerList(Integer questionnaireId, CurrentUser user) {
        UserAnswerDTO userAnswerList = userAnswerService.getUserAnswerList(questionnaireId, user);
        UserAnswerProgress userAnswerProgress = userAnswerProgressService.findOne(
                new UserAnswerProgress()
                        .setUserId(user.getExQuestionnaireUserId())
                        .setUserType(user.getQuestionnaireUserType()));
        if (Objects.nonNull(userAnswerProgress)) {
            userAnswerList.setCurrentSideBar(userAnswerProgress.getCurrentSideBar());
            userAnswerList.setCurrentStep(userAnswerProgress.getCurrentStep());
        }
        return userAnswerList;
    }

    /**
     * 保存答案
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveUserAnswer(UserAnswerDTO requestDTO, CurrentUser user) {
        Integer questionnaireId = requestDTO.getQuestionnaireId();
        List<UserAnswerDTO.QuestionDTO> questionList = requestDTO.getQuestionList();
        Integer userId = user.getExQuestionnaireUserId();
        Integer questionnaireUserType = user.getQuestionnaireUserType();

        IUserAnswerService iUserAnswerService = userAnswerFactory.getUserAnswerService(questionnaireUserType);
        // 更新记录表
        Integer recordId = iUserAnswerService.saveUserQuestionRecord(questionnaireId, userId, requestDTO.getIsFinish(), requestDTO.getQuestionnaireIds());

        // 先删除，后新增
        iUserAnswerService.deletedUserAnswer(questionnaireId, userId, questionList);

        // 保存用户答案
        iUserAnswerService.saveUserAnswer(requestDTO, userId, recordId);

        // 保存进度
        iUserAnswerService.saveUserProgress(requestDTO, userId, requestDTO.getIsFinish());

        // 处理隐藏问题
        iUserAnswerService.hiddenQuestion(questionnaireId, userId, recordId);

        // 获取用户答题状态
        return iUserAnswerService.getUserAnswerIsFinish(userId);
    }

    /**
     * 是否完成问卷
     *
     * @param user 用户
     *
     * @return Boolean
     */
    public Boolean userAnswerIsFinish(CurrentUser user) {
        IUserAnswerService iUserAnswerService = userAnswerFactory.getUserAnswerService(user.getQuestionnaireUserType());
        return iUserAnswerService.getUserAnswerIsFinish(user.getExQuestionnaireUserId());
    }

    /**
     * 获取学校名称
     *
     * @param user 用户
     *
     * @return 学校名称
     */
    public String getSchoolName(CurrentUser user) {
        IUserAnswerService iUserAnswerService = userAnswerFactory.getUserAnswerService(user.getQuestionnaireUserType());
        return iUserAnswerService.getSchoolName(user.getExQuestionnaireUserId());
    }

}
