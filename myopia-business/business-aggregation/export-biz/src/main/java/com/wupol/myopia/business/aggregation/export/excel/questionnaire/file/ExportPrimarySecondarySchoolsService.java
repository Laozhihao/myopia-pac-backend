package com.wupol.myopia.business.aggregation.export.excel.questionnaire.file;

import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.questionnaire.facade.QuestionnaireFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 导出中小学校开展学校卫生工作情况调查表
 *
 * @author hang.yuan 2022/7/18 11:24
 */
@Service
public class ExportPrimarySecondarySchoolsService implements QuestionnaireExcel {
    @Autowired
    private QuestionnaireFacade questionnaireFacade;

    @Override
    public Integer getType() {
        return QuestionnaireTypeEnum.PRIMARY_SECONDARY_SCHOOLS.getType();
    }

    @Override
    public List<List<String>> getHead(Integer questionnaireId) {
        return questionnaireFacade.getHead(questionnaireId);
    }

}
