package com.wupol.myopia.business.core.questionnaire.domain.mapper;

import com.wupol.myopia.business.core.questionnaire.domain.dto.QuestionnaireResponseDTO;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Mapper接口
 *
 * @Author Simple4H
 * @Date 2022-07-06
 */
public interface QuestionnaireMapper extends BaseMapper<Questionnaire> {

    QuestionnaireResponseDTO getQuestionnaireResponseById(@Param("id") Integer id);

    List<Questionnaire> getLatestData();

}
