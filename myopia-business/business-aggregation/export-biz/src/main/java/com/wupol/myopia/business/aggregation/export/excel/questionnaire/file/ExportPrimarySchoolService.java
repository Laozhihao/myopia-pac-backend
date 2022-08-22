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
import com.wupol.myopia.rec.domain.RecExportDTO;
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
    private UserAnswerRecFacade userAnswerRecFacade;

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
        List<GenerateRecDataBO> generateRecDataBOList = userAnswerRecFacade.generateRecData(buildGenerateDataCondition(exportCondition, Boolean.TRUE));
        if (CollUtil.isEmpty(generateRecDataBOList)){
            return;
        }
        for (GenerateRecDataBO generateRecDataBO : generateRecDataBOList) {
           userAnswerRecFacade.exportRecFile(fileName, buildRecExportDTO(generateRecDataBO));
        }
    }

    private RecExportDTO buildRecExportDTO(GenerateRecDataBO generateRecDataBO) {
        String recFileName = userAnswerRecFacade.getRecFileName(generateRecDataBO.getSchoolId(), getType());
        RecExportDTO recExportDTO = new RecExportDTO();
        recExportDTO.setQesUrl(generateRecDataBO.getQesUrl());
        recExportDTO.setDataList(generateRecDataBO.getDataList());
        recExportDTO.setRecName(recFileName);
        return recExportDTO;
    }


    @Override
    public GenerateDataCondition buildGenerateDataCondition(ExportCondition exportCondition, Boolean isAsc) {
        return new GenerateDataCondition()
                .setMainBodyType(QuestionnaireTypeEnum.PRIMARY_SCHOOL)
                .setBaseInfoType(QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE)
                .setGradeTypeList(Lists.newArrayList(SchoolAge.PRIMARY.code))
                .setExportCondition(exportCondition)
                .setIsAsc(isAsc);
    }
}
