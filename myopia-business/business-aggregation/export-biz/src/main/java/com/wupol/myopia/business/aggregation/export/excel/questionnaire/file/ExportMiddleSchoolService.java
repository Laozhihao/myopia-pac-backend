package com.wupol.myopia.business.aggregation.export.excel.questionnaire.file;

import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.questionnaire.domain.dto.ExcelDataConditionBO;
import com.wupol.myopia.business.core.questionnaire.facade.QuestionnaireFacade;
import com.wupol.myopia.business.core.questionnaire.facade.UserAnswerFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * 导出学生健康状况及影响因素调查表（中学版）
 *
 * @author hang.yuan 2022/7/20 11:26
 */
@Service
public class ExportMiddleSchoolService implements QuestionnaireExcel{

    @Autowired
    private QuestionnaireFacade questionnaireFacade;
    @Autowired
    private UserAnswerFacade userAnswerFacade;

    @Override
    public Integer getType() {
        return QuestionnaireTypeEnum.MIDDLE_SCHOOL.getType();
    }

    @Override
    public List<List<String>> getHead(Integer questionnaireId) {
        return questionnaireFacade.getHead(questionnaireId);
    }

    @Override
    public void generateExcelFile(ExportCondition exportCondition,String fileName) throws IOException {
        System.out.println("中学版");
    }

    @Override
    public void getExcelData(ExcelDataConditionBO excelDataConditionBO, List dataList) {
    }
}
