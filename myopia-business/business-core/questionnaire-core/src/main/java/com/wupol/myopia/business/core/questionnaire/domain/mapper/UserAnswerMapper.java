package com.wupol.myopia.business.core.questionnaire.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserAnswer;
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

    void deleteBatchByCombinationId(@Param("questionnaireId") Integer questionnaireId, @Param("userId") Integer userId, @Param("questionIds") List<Integer> questionIds);

}
