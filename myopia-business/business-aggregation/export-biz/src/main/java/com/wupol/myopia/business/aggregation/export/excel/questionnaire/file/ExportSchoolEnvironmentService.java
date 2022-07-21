package com.wupol.myopia.business.aggregation.export.excel.questionnaire.file;

import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.questionnaire.facade.QuestionnaireFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 导出学校环境健康影响因素调查表
 *
 * @author hang.yuan 2022/7/18 11:25
 */
@Service
public class ExportSchoolEnvironmentService implements QuestionnaireExcel {
    @Autowired
    private QuestionnaireFacade questionnaireFacade;

    @Override
    public Integer getType() {
        return QuestionnaireTypeEnum.SCHOOL_ENVIRONMENT.getType();
    }

    @Override
    public List<List<String>> getHead(Integer questionnaireId) {
        return questionnaireFacade.getHead(questionnaireId);
    }

}
