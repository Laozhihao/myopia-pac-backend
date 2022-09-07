package com.wupol.myopia.business.aggregation.export.excel.questionnaire.file;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.GenerateDataCondition;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.GenerateExcelDataBO;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.GenerateRecDataBO;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.FileNameCondition;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.QuestionnaireFactory;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.UserAnswerFacade;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.answer.Answer;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.common.utils.util.FileUtils;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * 导出地市及区（县）管理部门学校卫生工作调查表
 *
 * @author hang.yuan 2022/7/18 11:23
 */
@Service
public class ExportAreaDistrictSchoolService implements QuestionnaireExcel {

    @Value("classpath:excel/ExportAreaDistrictSchoolTemplate.xlsx")
    private Resource exportAreaDistrictSchoolTemplate;

    @Autowired
    private QuestionnaireFactory questionnaireFactory;
    @Autowired
    private UserAnswerFacade userAnswerFacade;


    @Override
    public Integer getType() {
        return QuestionnaireTypeEnum.AREA_DISTRICT_SCHOOL.getType();
    }

    @Override
    public void generateExcelFile(ExportCondition exportCondition, String fileName) throws IOException {
        Answer answerService = getAnswerService();
        GenerateDataCondition generateDataCondition = buildGenerateDataCondition(exportCondition);
        generateDataCondition.setFileType(QuestionnaireConstant.EXCEL_FILE);
        List<GenerateExcelDataBO> generateExcelDataBOList = answerService.getExcelData(generateDataCondition);
        if (CollUtil.isEmpty(generateExcelDataBOList)){
            return;
        }

        generateExcelDataBOList = userAnswerFacade.convertGovernmentValue(generateExcelDataBOList);

        for (GenerateExcelDataBO generateExcelDataBO : generateExcelDataBOList) {
            String governmentKey = generateExcelDataBO.getGovernmentKey();
            String[] key = governmentKey.split(StrUtil.UNDERLINE);
            String excelFileName = answerService.getFileName(buildFileNameCondition(Long.valueOf(key[2]), QuestionnaireConstant.EXCEL_FILE));
            String file = FileUtils.getFileSavePath(fileName, excelFileName);
            ExcelUtil.exportExcel(file, exportAreaDistrictSchoolTemplate.getInputStream(),generateExcelDataBO.getDataList());
        }
    }

    private Answer getAnswerService(){
        return questionnaireFactory.getAnswerService(UserType.QUESTIONNAIRE_GOVERNMENT.getType());
    }

    @Override
    public void generateRecFile(ExportCondition exportCondition, String fileName) {
        Answer answerService = getAnswerService();
        GenerateDataCondition generateDataCondition = buildGenerateDataCondition(exportCondition);
        generateDataCondition.setFileType(QuestionnaireConstant.REC_FILE);
        List<GenerateRecDataBO> generateRecDataBOList = answerService.getRecData(generateDataCondition);
        if (CollUtil.isEmpty(generateRecDataBOList)){
            return;
        }

        for (GenerateRecDataBO generateRecDataBO : generateRecDataBOList) {
            String governmentKey = generateRecDataBO.getGovernmentKey();
            String[] key = governmentKey.split(StrUtil.UNDERLINE);
            String recFileName = answerService.getFileName(buildFileNameCondition(Long.valueOf(key[2]), QuestionnaireConstant.REC_FILE));
            answerService.exportRecFile(fileName, generateRecDataBO,recFileName);
        }
    }


    /**
     * 构建文件名条件对象
     * @param districtCode 区域编码
     * @param fileType 文件类型
     */
    private FileNameCondition buildFileNameCondition(Long districtCode,String fileType){
        return new FileNameCondition()
                .setDistrictCode(districtCode)
                .setQuestionnaireType(getType())
                .setFileType(fileType);
    }


    @Override
    public GenerateDataCondition buildGenerateDataCondition(ExportCondition exportCondition) {
        return new GenerateDataCondition()
                .setMainBodyType(QuestionnaireTypeEnum.AREA_DISTRICT_SCHOOL)
                .setExportCondition(exportCondition)
                .setIsScore(Boolean.FALSE)
                .setUserType(UserType.QUESTIONNAIRE_GOVERNMENT.getType());
    }
}
