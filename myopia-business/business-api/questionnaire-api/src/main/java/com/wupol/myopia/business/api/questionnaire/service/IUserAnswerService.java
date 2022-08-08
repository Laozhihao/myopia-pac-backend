package com.wupol.myopia.business.api.questionnaire.service;

import com.wupol.myopia.business.core.questionnaire.domain.dto.UserAnswerDTO;
import com.wupol.myopia.business.core.questionnaire.domain.dto.UserQuestionnaireResponseDTO;

import java.util.List;

/**
 * 用户答案
 *
 * @author Simple4H
 */
public interface IUserAnswerService {

    /**
     * 获取用户类型
     *
     * @return 用户类型
     */
    Integer getUserType();

    /**
     * 保存用户问卷记录
     *
     * @param questionnaireId  问卷ID
     * @param userId           用户Id
     * @param isFinish         是否完成
     * @param questionnaireIds 问卷ID列表
     *
     * @return 记录Id
     */
    Integer saveUserQuestionRecord(Integer questionnaireId, Integer userId, Boolean isFinish, List<Integer> questionnaireIds);

    /**
     * 删除用户答案
     *
     * @param questionnaireId 问卷ID
     * @param userId          用户Id
     * @param questionList    问题列表
     */
    void deletedUserAnswer(Integer questionnaireId, Integer userId, List<UserAnswerDTO.QuestionDTO> questionList);

    /**
     * 保存用户答案
     *
     * @param requestDTO 用户答案DTO
     * @param userId     用户Id
     * @param recordId   记录Id
     */
    void saveUserAnswer(UserAnswerDTO requestDTO, Integer userId, Integer recordId);

    /**
     * 保存用户进度
     *
     * @param requestDTO 用户答案DTO
     * @param userId     用户Id
     * @param isFinish   是否完成
     */
    void saveUserProgress(UserAnswerDTO requestDTO, Integer userId, Boolean isFinish);

    /**
     * 获取用户问卷答案
     *
     * @param userId 用户Id
     *
     * @return 问卷答案列表
     */
    List<UserQuestionnaireResponseDTO> getUserQuestionnaire(Integer userId);

    /**
     * 获取用户是否完成问卷
     *
     * @param userId 用户Id
     *
     * @return 是否完成问卷
     */
    Boolean getUserAnswerIsFinish(Integer userId);

    /**
     * 获取学校名称
     *
     * @param userId 用户Id
     *
     * @return 学校名称
     */
    String getUserName(Integer userId);

    /**
     * 设置隐藏题目
     *
     * @param questionnaireId 问卷ID
     * @param userId          用户Id
     * @param recordId        记录Id
     */
    default void hiddenQuestion(Integer questionnaireId, Integer userId, Integer recordId) {
    }
}
