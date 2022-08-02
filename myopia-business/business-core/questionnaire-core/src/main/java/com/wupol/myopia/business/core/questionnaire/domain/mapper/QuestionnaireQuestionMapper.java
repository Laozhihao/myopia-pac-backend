package com.wupol.myopia.business.core.questionnaire.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQuestion;
import org.apache.ibatis.annotations.Param;


/**
 * Mapper接口
 *
 * @Author Simple4H
 * @Date 2022-07-06
 */
public interface QuestionnaireQuestionMapper extends BaseMapper<QuestionnaireQuestion> {

    void deletedLogic(@Param("questionnaireId") Integer questionnaireId, @Param("questionId") Integer questionId);
}
