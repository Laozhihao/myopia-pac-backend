package com.wupol.myopia.business.aggregation.export.excel.questionnaire.file;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.business.aggregation.export.excel.domain.GenerateDataCondition;
import com.wupol.myopia.business.aggregation.export.excel.domain.GenerateExcelDataBO;
import com.wupol.myopia.business.aggregation.export.excel.domain.GenerateRecDataBO;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.UserAnswerFacade;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.UserAnswerRecFacade;
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
 * 导出学生健康状况及影响因素调查表（大学版）
 *
 * @author hang.yuan 2022/7/20 11:26
 */
@Slf4j
@Service
public class ExportUniversitySchoolService implements QuestionnaireExcel {

    @Autowired
    private UserAnswerFacade userAnswerFacade;
    @Autowired
    private UserAnswerRecFacade userAnswerRecFacade;

    @Override
    public Integer getType() {
        return QuestionnaireTypeEnum.UNIVERSITY_SCHOOL.getType();
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

        List<GenerateRecDataBO> generateRecDataBOList = userAnswerRecFacade.generateRecData(buildGenerateDataCondition(exportCondition, Boolean.TRUE));
        if (CollUtil.isEmpty(generateRecDataBOList)){
            return;
        }
//        for (GenerateRecDataBO generateRecDataBO : generateRecDataBOList) {
//            userAnswerRecFacade.exportRecFile(fileName, generateRecDataBO,getType());
//        }
    }

    @Override
    public GenerateDataCondition buildGenerateDataCondition(ExportCondition exportCondition,Boolean isAsc){
        return new GenerateDataCondition()
                .setMainBodyType(QuestionnaireTypeEnum.UNIVERSITY_SCHOOL)
                .setBaseInfoType(QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE)
                .setGradeTypeList(Lists.newArrayList(SchoolAge.UNIVERSITY.code))
                .setExportCondition(exportCondition)
                .setIsAsc(isAsc);
    }
}
