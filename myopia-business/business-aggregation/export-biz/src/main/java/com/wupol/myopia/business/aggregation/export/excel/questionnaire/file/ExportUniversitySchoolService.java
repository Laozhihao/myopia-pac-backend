package com.wupol.myopia.business.aggregation.export.excel.questionnaire.file;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.FileNameCondition;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.GenerateDataCondition;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.UserAnswerFacade;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 导出学生健康状况及影响因素调查表（大学版）
 *
 * @author hang.yuan 2022/7/20 11:26
 */
@Slf4j
@Service
public class ExportUniversitySchoolService implements QuestionnaireExcel {

    @Value("classpath:excel/ExportUniversitySchoolTemplate.xlsx")
    private Resource exportUniversitySchoolTemplate;

    @Autowired
    private UserAnswerFacade userAnswerFacade;

    @Override
    public Integer getType() {
        return QuestionnaireTypeEnum.UNIVERSITY_SCHOOL.getType();
    }


    @Override
    public void generateExcelFile(ExportCondition exportCondition, String fileName) throws IOException {
        userAnswerFacade.generateStudentExcel(
                buildGenerateDataCondition(exportCondition),
                buildFileNameCondition(QuestionnaireConstant.EXCEL_FILE),
                exportUniversitySchoolTemplate,
                fileName);
    }


    @Override
    public void generateRecFile(ExportCondition exportCondition, String fileName) {
        userAnswerFacade.generateStudentRec(
                buildGenerateDataCondition(exportCondition),
                buildFileNameCondition(QuestionnaireConstant.REC_FILE),
                fileName);
    }

    /**
     * 构建文件名条件对象
     * @param fileType 文件类型
     */
    private FileNameCondition buildFileNameCondition(String fileType){
        return new FileNameCondition()
                .setQuestionnaireType(getType())
                .setFileType(fileType);
    }

    @Override
    public GenerateDataCondition buildGenerateDataCondition(ExportCondition exportCondition){
        return new GenerateDataCondition()
                .setMainBodyType(QuestionnaireTypeEnum.UNIVERSITY_SCHOOL)
                .setBaseInfoType(QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE)
                .setGradeTypeList(Lists.newArrayList(SchoolAge.UNIVERSITY.code))
                .setExportCondition(exportCondition)
                .setIsScore(Boolean.TRUE)
                .setUserType(UserType.QUESTIONNAIRE_STUDENT.getType());
    }
}
