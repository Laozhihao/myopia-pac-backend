package com.wupol.myopia.business.core.questionnaire.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.questionnaire.domain.mapper.QuestionnaireMapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author Simple4H
 * @Date 2022-07-06
 */
@Service
public class QuestionnaireService extends BaseService<QuestionnaireMapper, Questionnaire> {

    /**
     * 获取最新问卷数据
     *
     * @return 问卷数据集合
     */
    public List<Questionnaire> getLatestData(){
       return baseMapper.getLatestData();
    }
}
