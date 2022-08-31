package com.wupol.myopia.business.aggregation.export.excel.questionnaire.file;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.GenerateDataCondition;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.GenerateExcelDataBO;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.GenerateRecDataBO;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.RecFileNameCondition;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.QuestionnaireFactory;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.UserAnswerFacade;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.answer.Answer;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 导出学生健康状况及影响因素调查表（小学版）
 *
 * @author hang.yuan 2022/7/20 11:26
 */
@Slf4j
@Service
public class ExportPrimarySchoolService implements QuestionnaireExcel {

    @Autowired
    private UserAnswerFacade userAnswerFacade;

    @Autowired
    private QuestionnaireFactory questionnaireFactory;

    @Override
    public Integer getType() {
        return QuestionnaireTypeEnum.PRIMARY_SCHOOL.getType();
    }


    @Override
    public void generateExcelFile(ExportCondition exportCondition, String fileName) throws IOException {
        GenerateExcelDataBO generateExcelDataBO = userAnswerFacade.generateStudentTypeExcelData(buildGenerateDataCondition(exportCondition,Boolean.TRUE));
        if (Objects.isNull(generateExcelDataBO)){
            return;
        }
        Map<Integer, List<List<String>>> dataMap = generateExcelDataBO.getDataMap();
        for (Map.Entry<Integer, List<List<String>>> entry : dataMap.entrySet()) {
            String excelFileName = userAnswerFacade.getExcelFileName(entry.getKey(), getType());
            String file = getFileSavePath(fileName, excelFileName);
            ExcelUtil.exportListToExcel(file, entry.getValue(), generateExcelDataBO.getHead());
        }
    }

    @Override
    public void generateRecFile(ExportCondition exportCondition, String fileName) {
        Answer answerService = questionnaireFactory.getAnswerService(UserType.QUESTIONNAIRE_STUDENT.getType());
        List<GenerateRecDataBO> generateRecDataBOList = answerService.getRecData(buildGenerateDataCondition(exportCondition, Boolean.TRUE));
        if (CollUtil.isEmpty(generateRecDataBOList)){
            return;
        }
        for (GenerateRecDataBO generateRecDataBO : generateRecDataBOList) {
            String recFileName = answerService.getRecFileName(new RecFileNameCondition(generateRecDataBO.getSchoolId(), getType()));
            answerService.exportRecFile(fileName, generateRecDataBO,recFileName);
        }
    }


    @Override
    public GenerateDataCondition buildGenerateDataCondition(ExportCondition exportCondition, Boolean isAsc) {
        return new GenerateDataCondition()
                .setMainBodyType(QuestionnaireTypeEnum.PRIMARY_SCHOOL)
                .setBaseInfoType(QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE)
                .setGradeTypeList(Lists.newArrayList(SchoolAge.PRIMARY.code))
                .setExportCondition(exportCondition)
                .setIsAsc(isAsc)
                .setUserType(UserType.QUESTIONNAIRE_STUDENT.getType());
    }
}
