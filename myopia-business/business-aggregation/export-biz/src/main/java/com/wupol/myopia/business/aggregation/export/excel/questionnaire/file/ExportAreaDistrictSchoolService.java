package com.wupol.myopia.business.aggregation.export.excel.questionnaire.file;

import cn.hutool.core.collection.CollUtil;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.business.aggregation.export.excel.domain.GenerateDataCondition;
import com.wupol.myopia.business.aggregation.export.excel.domain.GenerateRecDataBO;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.QuestionnaireFactory;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.answer.Answer;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

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


    @Override
    public Integer getType() {
        return QuestionnaireTypeEnum.AREA_DISTRICT_SCHOOL.getType();
    }


    @Override
    public void generateRecFile(ExportCondition exportCondition, String fileName) {
        Answer answerService = questionnaireFactory.getAnswerService(UserType.QUESTIONNAIRE_GOVERNMENT.getType());
        List<GenerateRecDataBO> generateRecDataBOList = answerService.getRecData(buildGenerateDataCondition(exportCondition, Boolean.TRUE));
        if (CollUtil.isEmpty(generateRecDataBOList)){
            return;
        }

        for (GenerateRecDataBO generateRecDataBO : generateRecDataBOList) {
            String recFileName = answerService.getRecFileName(generateRecDataBO.getSchoolId(), getType());
            answerService.exportRecFile(fileName, generateRecDataBO,recFileName);
        }
    }

    @Override
    public GenerateDataCondition buildGenerateDataCondition(ExportCondition exportCondition, Boolean isAsc) {
        return new GenerateDataCondition()
                .setMainBodyType(QuestionnaireTypeEnum.AREA_DISTRICT_SCHOOL)
                .setExportCondition(exportCondition)
                .setIsAsc(isAsc)
                .setUserType(UserType.QUESTIONNAIRE_GOVERNMENT.getType());
    }
}
