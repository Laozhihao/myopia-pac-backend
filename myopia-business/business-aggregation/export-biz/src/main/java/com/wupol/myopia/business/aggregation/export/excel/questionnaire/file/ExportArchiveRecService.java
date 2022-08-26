package com.wupol.myopia.business.aggregation.export.excel.questionnaire.file;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.excel.domain.GenerateRecDataBO;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.ArchiveRecData;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.QuestionnaireFactory;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.answer.Answer;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.function.ExportType;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolTypeEnum;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQes;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireQesService;
import com.wupol.myopia.business.core.questionnaire.util.EpiDataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 导出监测表rec
 *
 * @author hang.yuan 2022/8/26 09:50
 */
@Service
public class ExportArchiveRecService implements QuestionnaireExcel{

    @Autowired
    private QuestionnaireFactory questionnaireFactory;
    @Autowired
    private ArchiveRecData archiveRecData;
    @Autowired
    private QuestionnaireQesService questionnaireQesService;
    @Autowired
    private ResourceFileService resourceFileService;

    @Override
    public Integer getType() {
        return QuestionnaireTypeEnum.ARCHIVE_REC.getType();
    }

    @Override
    public void generateRecFile(ExportCondition exportCondition, String fileName) {
        Answer answerService = questionnaireFactory.getAnswerService(UserType.QUESTIONNAIRE_STUDENT.getType());

        ExportType exportTypeService = questionnaireFactory.getExportTypeService(exportCondition.getExportType());
        List<ArchiveRecData.RecData> recDataList = archiveRecData.getDataMap(exportTypeService.getLockKey(exportCondition));
        Map<Integer, List<ArchiveRecData.RecData>> schoolDataMap = recDataList.stream().collect(Collectors.groupingBy(ArchiveRecData.RecData::getSchoolType));

        List<GenerateRecDataBO> generateRecDataBOList = Lists.newArrayList();
        Map<Integer, String> qesUrlMap = getQesUrl();
        schoolDataMap.forEach((schoolType,data)->{
            GenerateRecDataBO generateRecDataBO = new GenerateRecDataBO();
            generateRecDataBO.setQesUrl(qesUrlMap.get(schoolType));
            List<List<String>> dataList = data.stream().flatMap(recData -> recData.getDataList().stream()).collect(Collectors.toList());
            List<String> dataTxt = EpiDataUtil.mergeDataTxt(data.get(0).getQesFieldList(), dataList);
            generateRecDataBO.setDataList(dataTxt);
            generateRecDataBOList.add(generateRecDataBO);
        });

        for (GenerateRecDataBO generateRecDataBO : generateRecDataBOList) {
            answerService.exportRecFile(fileName,generateRecDataBO,QuestionnaireTypeEnum.ARCHIVE_REC.getDesc());
        }

    }

    private Map<Integer,String> getQesUrl() {
        List<QuestionnaireQes> archiveQesList = questionnaireQesService.getArchiveQesByName(QuestionnaireTypeEnum.ARCHIVE_REC.getDesc());
        if (CollUtil.isEmpty(archiveQesList)){
            throw new BusinessException(String.format("未上传QES文件,问卷类型:%s",QuestionnaireTypeEnum.ARCHIVE_REC.getDesc()));
        }

        List<QuestionnaireQes> notQesFileIdList = archiveQesList.stream().filter(questionnaireQes -> Objects.isNull(questionnaireQes.getQesFileId())).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(notQesFileIdList)){
            throw new BusinessException(String.format("%s未上传QES文件",CollUtil.join(notQesFileIdList.stream().map(QuestionnaireQes::getName).collect(Collectors.toList()), StrUtil.COMMA)));
        }

        Map<Integer,String> qesUrlMap = Maps.newHashMap();
        for (QuestionnaireQes questionnaireQes : archiveQesList) {
            String qesUrl = resourceFileService.getResourcePath(questionnaireQes.getQesFileId());
            if (questionnaireQes.getName().contains(SchoolTypeEnum.KINDERGARTEN.getDesc())){
                qesUrlMap.put(SchoolTypeEnum.KINDERGARTEN.getType(),qesUrl);
            }
            if (questionnaireQes.getName().contains(SchoolTypeEnum.PRIMARY_AND_SECONDARY.getDesc())){
                qesUrlMap.put(SchoolTypeEnum.PRIMARY_AND_SECONDARY.getType(),qesUrl);
            }
            if (questionnaireQes.getName().contains(SchoolTypeEnum.UNIVERSITY.getDesc())){
                qesUrlMap.put(SchoolTypeEnum.UNIVERSITY.getType(),qesUrl);
            }
        }

        return qesUrlMap;
    }
}
