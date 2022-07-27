package com.wupol.myopia.business.aggregation.export.excel.questionnaire.file;

import com.google.common.collect.Maps;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.QuestionnaireFacade;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.UserAnswerFacade;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    public List<List<String>> getHead(List<Integer> questionnaireIds) {
        return questionnaireFacade.getHead(questionnaireIds);
    }

    @Override
    public void generateExcelFile(ExportCondition exportCondition,String fileName) throws IOException {
        List<Integer> questionnaireTypeList = questionnaireFacade.getQuestionnaireTypeList(QuestionnaireTypeEnum.MIDDLE_SCHOOL);

        List<UserQuestionRecord> userQuestionRecordList = userAnswerFacade.getQuestionnaireRecordList(exportCondition, questionnaireTypeList);
        if (CollectionUtils.isEmpty(userQuestionRecordList)){
            return;
        }

        List<Questionnaire> questionnaireList = questionnaireFacade.getLatestQuestionnaire(questionnaireTypeList);
        if (CollectionUtils.isEmpty(questionnaireList)){
            return;
        }
        // QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE 类型的问卷ID
        Integer questionnaireId = questionnaireList.stream()
                .filter(questionnaire -> Objects.equals(questionnaire.getType(), QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE.getType()))
                .findFirst().map(Questionnaire::getId).orElse(null);

        List<Integer> latestQuestionnaireIds =   questionnaireList.stream().sorted(Comparator.comparing(Questionnaire::getType)).map(Questionnaire::getId).collect(Collectors.toList());
        Map<Integer, List<UserQuestionRecord>> schoolRecordMap = userQuestionRecordList.stream()
                .filter(userQuestionRecord -> latestQuestionnaireIds.contains(userQuestionRecord.getQuestionnaireId()))
                .sorted(Comparator.comparing(UserQuestionRecord::getId))
                .collect(Collectors.groupingBy(UserQuestionRecord::getSchoolId));

        Map<Integer,List> dataMap= Maps.newHashMap();

        for (Map.Entry<Integer, List<UserQuestionRecord>> entry : schoolRecordMap.entrySet()) {
            dataMap.put(entry.getKey(), userAnswerFacade.getData(entry.getValue(), latestQuestionnaireIds,questionnaireId));
        }

        for (Map.Entry<Integer, List<UserQuestionRecord>> entry : schoolRecordMap.entrySet()) {
            String excelFileName = questionnaireFacade.getExcelFileName(entry.getKey(), getType());
            String file = getFileSavePath(fileName, excelFileName);
            ExcelUtil.exportListToExcel(file, dataMap.get(entry.getKey()), getHead(latestQuestionnaireIds));
        }

    }

}
