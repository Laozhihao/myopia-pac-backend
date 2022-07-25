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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 导出学生健康状况及影响因素调查表（小学版）
 *
 * @author hang.yuan 2022/7/20 11:26
 */
@Service
public class ExportPrimarySchoolService implements QuestionnaireExcel {

    @Autowired
    private QuestionnaireFacade questionnaireFacade;
    @Autowired
    private UserAnswerFacade userAnswerFacade;

    @Override
    public Integer getType() {
        return QuestionnaireTypeEnum.PRIMARY_SCHOOL.getType();
    }

    @Override
    public List<List<String>> getHead(Integer questionnaireId) {
        return questionnaireFacade.getHead(questionnaireId);
    }

    @Override
    public void generateExcelFile(ExportCondition exportCondition,String fileName) throws IOException {

        List<UserQuestionRecord> userQuestionRecordList = userAnswerFacade.getQuestionnaireRecordList(exportCondition.getPlanId(), QuestionnaireTypeEnum.PRIMARY_SCHOOL.getType());
        if (CollectionUtils.isEmpty(userQuestionRecordList)){
            return;
        }
        List<Integer> latestQuestionnaireIds = questionnaireFacade.getLatestQuestionnaireIds();

        Map<Integer, List<UserQuestionRecord>> questionnaireMap = userQuestionRecordList.stream()
                .filter(userQuestionRecord -> latestQuestionnaireIds.contains(userQuestionRecord.getQuestionnaireId()))
                .collect(Collectors.groupingBy(UserQuestionRecord::getQuestionnaireId));

        ExcelDataConditionBO excelDataConditionBO = new ExcelDataConditionBO();
        excelDataConditionBO.setQuestionnaireType(QuestionnaireTypeEnum.PRIMARY_SCHOOL.getType());
        excelDataConditionBO.setScreeningPlanId(exportCondition.getPlanId());

        for (Map.Entry<Integer, List<UserQuestionRecord>> entry : questionnaireMap.entrySet()) {
            excelDataConditionBO.setQuestionnaireId(entry.getKey());
            List dataList = Lists.newArrayList();
            getExcelData(excelDataConditionBO, dataList);
            ExcelUtil.exportListToExcel(null, dataList, getHead(entry.getKey()));
        }

    }

    @Override
    public void getExcelData(ExcelDataConditionBO excelDataConditionBO, List dataList) {
        List excelData = userAnswerFacade.getExcelData(excelDataConditionBO);
        if (!CollectionUtils.isEmpty(excelData)){
            dataList.addAll(excelData);
        }
    }
}
