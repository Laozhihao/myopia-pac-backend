package com.wupol.myopia.business.core.questionnaire.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.core.questionnaire.domain.dto.EditQuestionnaireRequestDTO;
import com.wupol.myopia.business.core.questionnaire.domain.mapper.QuestionnaireMapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @Author Simple4H
 * @Date 2022-07-06
 */
@Service
public class QuestionnaireService extends BaseService<QuestionnaireMapper, Questionnaire> {

    @Autowired
    private QuestionnaireQuestionService questionnaireQuestionService;

    /**
     * 获取问卷列表
     *
     * @param year 年份
     *
     * @return List<Questionnaire>
     */
    public List<Questionnaire> getQuestionnaireList(Integer year) {
        // 默认今年
        if (Objects.isNull(year)) {
            year = DateUtil.getYear(new Date());
        }
        return getByYear(year);
    }

    /**
     * 通过年份获取
     *
     * @param year 年份
     *
     * @return List<Questionnaire>
     */
    public List<Questionnaire> getByYear(Integer year) {
        LambdaQueryWrapper<Questionnaire> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Questionnaire::getYear, year);
        return baseMapper.selectList(wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public void editQuestionnaire(EditQuestionnaireRequestDTO requestDTO) {
        Integer questionnaireId = requestDTO.getQuestionnaireId();
        questionnaireQuestionService.deletedByQuestionnaireId(questionnaireId);
        questionnaireQuestionService.insert(questionnaireId, requestDTO.getDetails(), -1);

    }

}
