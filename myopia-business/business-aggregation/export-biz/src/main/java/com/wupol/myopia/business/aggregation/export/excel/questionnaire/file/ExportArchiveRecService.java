package com.wupol.myopia.business.aggregation.export.excel.questionnaire.file;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.GenerateRecDataBO;
import com.wupol.myopia.business.aggregation.export.excel.domain.builder.ArchiveDataFieldBuilder;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.AnswerFactory;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.answer.Answer;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.aggregation.export.service.ArchiveRecDataBuilder;
import com.wupol.myopia.business.aggregation.export.service.ArchiveService;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolTypeEnum;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QesFieldDataBO;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQes;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireQesService;
import com.wupol.myopia.business.core.questionnaire.util.AnswerUtil;
import com.wupol.myopia.business.core.questionnaire.util.EpiDataUtil;
import com.wupol.myopia.business.core.screening.flow.domain.vo.CardInfoVO;
import com.wupol.myopia.business.core.screening.flow.domain.vo.CommonDiseaseArchiveCard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 导出监测表rec
 *
 * @author hang.yuan 2022/8/26 09:50
 */
@Slf4j
@Service
public class ExportArchiveRecService implements QuestionnaireExcel {

    @Autowired
    private AnswerFactory answerFactory;
    @Autowired
    private ArchiveService archiveService;
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
        Answer answerService = answerFactory.getAnswerService(UserType.QUESTIONNAIRE_STUDENT.getType());

        List<CommonDiseaseArchiveCard> archiveData = archiveService.getArchiveData(exportCondition);

        Map<Integer, List<CommonDiseaseArchiveCard>> schoolTypeMap = getSchoolTypeMap(archiveData);
        Map<Integer, String> qesUrlMap = getQesUrl();

        List<GenerateRecDataBO> generateRecDataBOList = Lists.newArrayList();

        schoolTypeMap.forEach((schoolType, dataList) -> {
            List<String> archiveQesFieldList = ArchiveDataFieldBuilder.getArchiveQesFieldList(schoolType);
            Map<Integer, List<CommonDiseaseArchiveCard>> schoolMap = dataList.stream().collect(Collectors.groupingBy(commonDiseaseArchiveCard -> commonDiseaseArchiveCard.getStudentInfo().getSchoolId()));
            schoolMap.forEach((schoolId, schoolDataList) -> buildGenerateRecDataBO(qesUrlMap, generateRecDataBOList, schoolType, dataList, archiveQesFieldList, schoolDataList));
        });

        for (GenerateRecDataBO generateRecDataBO : generateRecDataBOList) {
            answerService.exportRecFile(fileName, generateRecDataBO, getFileName(generateRecDataBO.getSchoolType(), generateRecDataBO.getSchoolName()));
        }

    }

    /**
     * 构建生成rec数据对象
     * @param qesUrlMap qes的地址集合
     * @param generateRecDataBOList 生成rec数据集合
     * @param schoolType 学校类型
     * @param dataList 数据集合
     * @param archiveQesFieldList 监测表qes字段集合
     * @param schoolDataList 学校数据集合
     */
    private void buildGenerateRecDataBO(Map<Integer, String> qesUrlMap, List<GenerateRecDataBO> generateRecDataBOList, Integer schoolType, List<CommonDiseaseArchiveCard> dataList, List<String> archiveQesFieldList, List<CommonDiseaseArchiveCard> schoolDataList) {
        GenerateRecDataBO generateRecDataBO = new GenerateRecDataBO();
        generateRecDataBO.setQesUrl(qesUrlMap.get(schoolType));
        generateRecDataBO.setSchoolType(schoolType);
        CommonDiseaseArchiveCard commonDiseaseArchiveCard = schoolDataList.get(0);
        List<List<QesFieldDataBO>> qesDataList = ArchiveRecDataBuilder.getDataList(schoolType, dataList);
        List<List<String>> qesAnswerDataList = qesDataList.stream().map(qesFieldDataBOList -> {
            Map<String, QesFieldDataBO> qesFieldDataBoMap = qesFieldDataBOList.stream().collect(Collectors.toMap(QesFieldDataBO::getQesField, Function.identity(), (v1, v2) -> v2));
            List<QesFieldDataBO> sortList = Lists.newArrayList();
            archiveQesFieldList.forEach(qesField -> sortList.add(qesFieldDataBoMap.get(qesField)));
            return sortList.stream().map(QesFieldDataBO::getRecAnswer).collect(Collectors.toList());
        }).collect(Collectors.toList());

        List<String> qesFieldList = archiveQesFieldList.stream()
                .map(AnswerUtil::getQesFieldStr)
                .collect(Collectors.toList());
        List<String> dataTxt = EpiDataUtil.mergeDataTxt(qesFieldList, qesAnswerDataList);
        generateRecDataBO.setDataList(dataTxt);
        generateRecDataBO.setSchoolName(commonDiseaseArchiveCard.getStudentInfo().getSchoolName());
        generateRecDataBOList.add(generateRecDataBO);
    }

    /**
     * 获取文件名称
     * @param schoolType 学校类型
     * @param schoolName 学校名称
     */
    private String getFileName(Integer schoolType, String schoolName) {

        if (Objects.equals(schoolType, SchoolTypeEnum.KINDERGARTEN.getType())) {
            return getFormatName(schoolName,SchoolTypeEnum.KINDERGARTEN.getDesc());
        }

        if (Objects.equals(schoolType, SchoolTypeEnum.PRIMARY_AND_SECONDARY.getType())) {
            return getFormatName(schoolName,SchoolTypeEnum.PRIMARY_AND_SECONDARY.getDesc());
        }

        if (Objects.equals(schoolType, SchoolTypeEnum.UNIVERSITY.getType())) {
            return getFormatName(schoolName,SchoolTypeEnum.UNIVERSITY.getDesc());
        }
        return StrUtil.EMPTY;

    }

    /**
     * 获取格式化后的名称
     * @param schoolName 学校名称
     * @param suffix 后缀
     */
    private String getFormatName(String schoolName,String suffix){
        String fileNameTmp = "%s的%s(%s)的rec文件";
        schoolName = FileNameUtil.cleanInvalid(schoolName);
        return String.format(fileNameTmp, schoolName, QuestionnaireTypeEnum.ARCHIVE_REC.getDesc(), suffix+ "版");
    }

    /**
     * 获取学校类型集合
     * @param archiveData 监测数据
     */
    private Map<Integer, List<CommonDiseaseArchiveCard>> getSchoolTypeMap(List<CommonDiseaseArchiveCard> archiveData) {
        Map<Integer, List<CommonDiseaseArchiveCard>> schoolTypeMap = Maps.newHashMap();
        for (CommonDiseaseArchiveCard archiveCard : archiveData) {
            CardInfoVO studentInfo = archiveCard.getStudentInfo();
            SchoolTypeEnum schoolType = SchoolTypeEnum.getByType(studentInfo.getSchoolType());
            List<CommonDiseaseArchiveCard> commonDiseaseArchiveCards = schoolTypeMap.get(schoolType.getType());
            if (CollUtil.isEmpty(commonDiseaseArchiveCards)) {
                commonDiseaseArchiveCards = Lists.newArrayList();
            }
            commonDiseaseArchiveCards.add(archiveCard);
            schoolTypeMap.put(schoolType.getType(), commonDiseaseArchiveCards);
        }
        return schoolTypeMap;
    }

    /**
     * 获取监测表qes文件地址集合
     */
    private Map<Integer, String> getQesUrl() {
        List<QuestionnaireQes> archiveQesList = questionnaireQesService.getArchiveQesByName(QuestionnaireTypeEnum.ARCHIVE_REC.getDesc());
        if (CollUtil.isEmpty(archiveQesList)) {
            throw new BusinessException(String.format("未上传QES文件,问卷类型:%s", QuestionnaireTypeEnum.ARCHIVE_REC.getDesc()));
        }

        List<QuestionnaireQes> notQesFileIdList = archiveQesList.stream().filter(questionnaireQes -> Objects.isNull(questionnaireQes.getQesFileId())).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(notQesFileIdList)) {
            throw new BusinessException(String.format("%s未上传QES文件", CollUtil.join(notQesFileIdList.stream().map(QuestionnaireQes::getName).collect(Collectors.toList()), StrUtil.COMMA)));
        }

        Map<Integer, String> qesUrlMap = Maps.newHashMap();
        for (QuestionnaireQes questionnaireQes : archiveQesList) {
            String qesUrl = resourceFileService.getResourcePath(questionnaireQes.getQesFileId());
            if (questionnaireQes.getName().contains(SchoolTypeEnum.KINDERGARTEN.getDesc())) {
                qesUrlMap.put(SchoolTypeEnum.KINDERGARTEN.getType(), qesUrl);
            }
            if (questionnaireQes.getName().contains(SchoolTypeEnum.PRIMARY_AND_SECONDARY.getDesc())) {
                qesUrlMap.put(SchoolTypeEnum.PRIMARY_AND_SECONDARY.getType(), qesUrl);
            }
            if (questionnaireQes.getName().contains(SchoolTypeEnum.UNIVERSITY.getDesc())) {
                qesUrlMap.put(SchoolTypeEnum.UNIVERSITY.getType(), qesUrl);
            }
        }

        return qesUrlMap;
    }
}
