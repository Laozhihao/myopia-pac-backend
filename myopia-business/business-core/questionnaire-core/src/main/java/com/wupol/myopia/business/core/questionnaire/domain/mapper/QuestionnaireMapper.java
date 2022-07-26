package com.wupol.myopia.business.core.questionnaire.domain.mapper;

import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

import java.util.Collection;
import java.util.List;

/**
 * Mapper接口
 *
 * @Author Simple4H
 * @Date 2022-07-06
 */
public interface QuestionnaireMapper extends BaseMapper<Questionnaire> {

    List<Questionnaire> getByTypes(@Param("types") Collection<Integer> types);

    List<Questionnaire> getLatestData();

}
