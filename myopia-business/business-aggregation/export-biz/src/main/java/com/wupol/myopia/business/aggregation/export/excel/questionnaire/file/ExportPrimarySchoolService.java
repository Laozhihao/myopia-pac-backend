package com.wupol.myopia.business.aggregation.export.excel.questionnaire.file;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.QuestionnaireExcelFacade;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.questionnaire.domain.dto.ExcelDataConditionBO;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.questionnaire.facade.QuestionnaireFacade;
import com.wupol.myopia.business.core.questionnaire.facade.UserAnswerFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Comparator;
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
    @Autowired
    private QuestionnaireExcelFacade questionnaireExcelFacade;

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
        System.out.println("小学");

        List<Integer> questionnaireTypeList = questionnaireFacade.getQuestionnaireTypeList(QuestionnaireTypeEnum.PRIMARY_SCHOOL);

        List<UserQuestionRecord> userQuestionRecordList = userAnswerFacade.getQuestionnaireRecordList(exportCondition.getPlanId(), questionnaireTypeList);
        if (CollectionUtils.isEmpty(userQuestionRecordList)){
            return;
        }
        List<Integer> latestQuestionnaireIds = questionnaireFacade.getLatestQuestionnaireIds(questionnaireTypeList);

        userQuestionRecordList = userQuestionRecordList.stream()
                .filter(userQuestionRecord -> latestQuestionnaireIds.contains(userQuestionRecord.getQuestionnaireId()))
                .sorted(Comparator.comparing(UserQuestionRecord::getId))
                .collect(Collectors.toList());

        questionnaireFacade.process(userQuestionRecordList);
        //基础问卷信息
//        List<UserQuestionRecord> baseInfoList = typeMap.get(QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE.getType());
        //小学板问卷信息
//        List<UserQuestionRecord> primarySchoolList = typeMap.get(QuestionnaireTypeEnum.PRIMARY_SCHOOL.getType());

//        userAnswerFacade.baseProcess(baseInfoList);

        ExcelDataConditionBO excelDataConditionBO = new ExcelDataConditionBO();
        excelDataConditionBO.setQuestionnaireTypeList(questionnaireTypeList);
        excelDataConditionBO.setScreeningPlanId(exportCondition.getPlanId());



    }

    @Override
    public void getExcelData(ExcelDataConditionBO excelDataConditionBO, List dataList) {
        List excelData = userAnswerFacade.getExcelData(excelDataConditionBO);
        if (!CollectionUtils.isEmpty(excelData)){
            dataList.addAll(excelData);
        }
    }

    private void excelFile(String schoolId,List dataList,Integer questionnaireId) throws IOException {
        String excelFileName = questionnaireExcelFacade.getExcelFileName(schoolId, questionnaireId);
        ExcelUtil.exportListToExcel(excelFileName, dataList, getHead(questionnaireId));
    }
}
