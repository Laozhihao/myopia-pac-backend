package com.wupol.myopia.business.aggregation.export.excel.questionnaire.file;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.questionnaire.domain.dto.ExcelDataConditionBO;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.questionnaire.facade.QuestionnaireFacade;
import com.wupol.myopia.business.core.questionnaire.facade.UserAnswerFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;

/**
 * 导出学生健康状况及影响因素调查表（大学版）
 *
 * @author hang.yuan 2022/7/20 11:26
 */
@Service
public class ExportUniversitySchoolService implements QuestionnaireExcel {
    @Autowired
    private QuestionnaireFacade questionnaireFacade;
    @Autowired
    private UserAnswerFacade userAnswerFacade;

    @Override
    public Integer getType() {
        return QuestionnaireTypeEnum.UNIVERSITY_SCHOOL.getType();
    }

    @Override
    public List<List<String>> getHead(Integer questionnaireId) {
        return questionnaireFacade.getHead(questionnaireId);
    }

    @Override
    public void generateExcelFile(ExportCondition exportCondition) throws IOException {
        List<UserQuestionRecord> userQuestionRecordList = userAnswerFacade.getQuestionnaireList(exportCondition.getPlanId(), QuestionnaireTypeEnum.MIDDLE_SCHOOL.getType());
        if (CollectionUtils.isEmpty(userQuestionRecordList)){
            return;
        }
        for (UserQuestionRecord userQuestionRecord : userQuestionRecordList) {
            List dataList = Lists.newArrayList();
            getExcelData(exportCondition, dataList);
            ExcelUtil.exportListToExcel(null, dataList, getHead(userQuestionRecord.getQuestionnaireId()));
        }
    }

    @Override
    public void getExcelData(ExportCondition exportCondition, List dataList) {
        List excelData = userAnswerFacade.getExcelData(new ExcelDataConditionBO());
        if (!CollectionUtils.isEmpty(excelData)){
            dataList.addAll(excelData);
        }
    }
}
