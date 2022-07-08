package com.wupol.myopia.business.core.questionnaire.domain.mapper;

import com.wupol.myopia.business.core.questionnaire.domain.model.UserAnswer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Mapper接口
 *
 * @Author Simple4H
 * @Date 2022-07-06
 */
public interface UserAnswerMapper extends BaseMapper<UserAnswer> {

    void batchSaveUserAnswer(@Param("userAnswers") List<UserAnswer> userAnswers);

    List<UserAnswer> getByQuestionnaireId(@Param("questionnaireId") Integer questionnaireId, @Param("userId") Integer userId);

}
