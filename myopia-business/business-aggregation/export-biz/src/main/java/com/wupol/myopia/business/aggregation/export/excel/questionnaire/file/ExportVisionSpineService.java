package com.wupol.myopia.business.aggregation.export.excel.questionnaire.file;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.GenerateDataCondition;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.GenerateExcelDataBO;
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
 * 导出学生视力不良及脊柱弯曲异常影响因素专项调查表
 *
 * @author hang.yuan 2022/7/18 11:24
 */
@Slf4j
@Service
public class ExportVisionSpineService implements QuestionnaireExcel {

    @Autowired
    private UserAnswerFacade userAnswerFacade;

    @Override
    public Integer getType() {
        return QuestionnaireTypeEnum.VISION_SPINE.getType();
    }


    @Override
    public void generateExcelFile(ExportCondition exportCondition, String fileName) throws IOException {

        GenerateExcelDataBO generateExcelDataBO = userAnswerFacade.generateStudentTypeExcelData(buildGenerateDataCondition(exportCondition,Boolean.FALSE));
        if (Objects.isNull(generateExcelDataBO)){
            return;
        }

//        Map<Integer, List<List<String>>> dataMap = generateExcelDataBO.getDataMap();
//        for (Map.Entry<Integer, List<List<String>>> entry : dataMap.entrySet()) {
//            String excelFileName = userAnswerFacade.getExcelFileName(entry.getKey(), getType());
//            String file = getFileSavePath(fileName, excelFileName);
//            ExcelUtil.exportListToExcel(file, entry.getValue(), generateExcelDataBO.getHead());
//        }
    }

    @Override
    public GenerateDataCondition buildGenerateDataCondition(ExportCondition exportCondition, Boolean isAsc) {
        return new GenerateDataCondition()
                .setMainBodyType(QuestionnaireTypeEnum.VISION_SPINE)
                .setBaseInfoType(QuestionnaireTypeEnum.VISION_SPINE_NOTICE)
                .setGradeTypeList(Lists.newArrayList(SchoolAge.PRIMARY.code,SchoolAge.JUNIOR.code,SchoolAge.HIGH.code,SchoolAge.VOCATIONAL_HIGH.code,SchoolAge.UNIVERSITY.code))
                .setExportCondition(exportCondition)
                .setIsAsc(isAsc);
    }
}
