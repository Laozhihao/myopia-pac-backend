package com.wupol.myopia.business.core.questionnaire.domain.mapper;

import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Mapper接口
 *
 * @Author Simple4H
 * @Date 2022-07-06
 */
public interface QuestionMapper extends BaseMapper<Question> {

    Question getByQuestionId(@Param("id") Integer id);

    List<Question> getByIds(@Param("ids") List<Integer> ids);
}
