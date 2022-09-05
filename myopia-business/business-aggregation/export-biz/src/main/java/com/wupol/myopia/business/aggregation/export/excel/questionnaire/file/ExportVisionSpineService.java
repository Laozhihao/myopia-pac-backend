package com.wupol.myopia.business.aggregation.export.excel.questionnaire.file;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.FileNameCondition;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.GenerateDataCondition;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.GenerateExcelDataBO;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.QuestionnaireFactory;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.UserAnswerFacade;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.answer.Answer;
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
import java.util.List;

/**
 * 导出学生视力不良及脊柱弯曲异常影响因素专项调查表
 *
 * @author hang.yuan 2022/7/18 11:24
 */
@Slf4j
@Service
public class ExportVisionSpineService implements QuestionnaireExcel {

    @Value("classpath:excel/ExportVisionSpineTemplate.xlsx")
    private Resource exportVisionSpineTemplate;

    @Autowired
    private UserAnswerFacade userAnswerFacade;

    @Autowired
    private QuestionnaireFactory questionnaireFactory;

    @Override
    public Integer getType() {
        return QuestionnaireTypeEnum.VISION_SPINE.getType();
    }


    @Override
    public void generateExcelFile(ExportCondition exportCondition, String fileName) throws IOException {
        Answer answerService = getAnswerService();
        List<GenerateExcelDataBO> generateExcelDataBOList = answerService.getExcelData(buildGenerateDataCondition(exportCondition, Boolean.TRUE));
        if (CollUtil.isEmpty(generateExcelDataBOList)){
            return;
        }

        generateExcelDataBOList = userAnswerFacade.convertStudentValue(generateExcelDataBOList);

        for (GenerateExcelDataBO generateExcelDataBO : generateExcelDataBOList) {
            String excelFileName = answerService.getFileName(buildFileNameCondition(generateExcelDataBO.getSchoolId(), QuestionnaireConstant.EXCEL_FILE));
            String file = getFileSavePath(fileName, excelFileName);
            ExcelUtil.exportExcel(file, exportVisionSpineTemplate.getInputStream(),generateExcelDataBO.getDataList());
        }
    }

    private Answer getAnswerService(){
        return questionnaireFactory.getAnswerService(UserType.QUESTIONNAIRE_STUDENT.getType());
    }


    private FileNameCondition buildFileNameCondition(Integer schoolId, String fileType){
        return new FileNameCondition()
                .setSchoolId(schoolId)
                .setQuestionnaireType(getType())
                .setFileType(fileType);
    }

    @Override
    public GenerateDataCondition buildGenerateDataCondition(ExportCondition exportCondition, Boolean isAsc) {
        return new GenerateDataCondition()
                .setMainBodyType(QuestionnaireTypeEnum.VISION_SPINE)
                .setBaseInfoType(QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE)
                .setGradeTypeList(Lists.newArrayList(SchoolAge.PRIMARY.code,SchoolAge.JUNIOR.code,SchoolAge.HIGH.code,SchoolAge.VOCATIONAL_HIGH.code,SchoolAge.UNIVERSITY.code))
                .setExportCondition(exportCondition)
                .setIsScore(Boolean.FALSE)
                .setUserType(UserType.QUESTIONNAIRE_STUDENT.getType());
    }
}
