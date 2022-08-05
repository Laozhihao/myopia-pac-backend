package com.wupol.myopia.business.core.questionnaire.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.questionnaire.domain.mapper.QuestionnaireQesMapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQes;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *  问卷QES文件管理
 *
 * @author hang.yuan
 * @date 2022/8/4
 */
@Service
public class QuestionnaireQesService extends BaseService<QuestionnaireQesMapper, QuestionnaireQes> {

    /**
     * 根据年份查询问卷模板QES集合
     * @param year 年份
     */
    public List<QuestionnaireQes> listByYear(Integer year){
        return baseMapper.selectList(new LambdaQueryWrapper<QuestionnaireQes>().eq(QuestionnaireQes::getYear,year));
    }
}
