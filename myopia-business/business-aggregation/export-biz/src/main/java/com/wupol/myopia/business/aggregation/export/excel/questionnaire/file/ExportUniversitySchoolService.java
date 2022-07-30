package com.wupol.myopia.business.aggregation.export.excel.questionnaire.file;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.business.aggregation.export.excel.domain.GenerateExcelDataBO;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.UserAnswerFacade;
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

    @Override
    public Integer getType() {
        return QuestionnaireTypeEnum.UNIVERSITY_SCHOOL.getType();
    }


    @Override
    public void generateExcelFile(ExportCondition exportCondition,String fileName) throws IOException {

        List<Integer> gradeTypeList = Lists.newArrayList(SchoolAge.UNIVERSITY.code);

        GenerateExcelDataBO generateExcelDataBO = userAnswerFacade.generateStudentTypeExcelData(QuestionnaireTypeEnum.MIDDLE_SCHOOL, QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE, gradeTypeList, exportCondition);
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

}
