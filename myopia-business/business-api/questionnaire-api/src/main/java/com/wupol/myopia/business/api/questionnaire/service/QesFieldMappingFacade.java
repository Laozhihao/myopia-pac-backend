package com.wupol.myopia.business.api.questionnaire.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.IOUtils;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import com.wupol.myopia.business.core.questionnaire.domain.model.QesFieldMapping;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQes;
import com.wupol.myopia.business.core.questionnaire.service.QesFieldMappingService;
import com.wupol.myopia.business.core.questionnaire.util.EpiDataUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * qes文件字段映射
 *
 * @author hang.yuan 2022/7/18 14:16
 */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class QesFieldMappingFacade {

    private final ResourceFileService resourceFileService;
    private final QesFieldMappingService qesFieldMappingService;


    /**
     * 保存qes字段映射
     * @param questionnaireQes 问卷qes管理对象
     */
    public void saveQesFieldMapping(QuestionnaireQes questionnaireQes) {
        if (Objects.isNull(questionnaireQes) || Objects.isNull(questionnaireQes.getQesFileId())){
            return;
        }
        String qesUrl = resourceFileService.getResourcePath(questionnaireQes.getQesFileId());
        List<String> variableList = Lists.newArrayList();
        String qesSavePath = getQesTmpSavePath();
        try {
            FileUtils.copyURLToFile(new URL(qesUrl), new File(qesSavePath));
            EpiDataUtil.qesToVariable(qesSavePath,variableList);
        } catch (IOException e) {
            throw new BusinessException("生成QES文件异常", e);
        }finally {
            FileUtil.del(qesSavePath);
        }

        if (CollectionUtils.isEmpty(variableList)){
            return;
        }

        qesFieldMappingService.remove(new QesFieldMapping().setQesId(questionnaireQes.getId()));

        List<QesFieldMapping> qesFieldMappingList = variableList.stream().map(variable -> buildQesFieldMapping(variable, questionnaireQes)).collect(Collectors.toList());
        qesFieldMappingService.saveBatch(qesFieldMappingList);
    }

    /**
     * 获取qes文件临时保存路径
     */
    private String getQesTmpSavePath(){
        String epiDataPath = IOUtils.getTempSubPath(QuestionnaireConstant.EPI_DATA_FOLDER);
        return Paths.get(epiDataPath, UUID.randomUUID().toString()+QuestionnaireConstant.getQesExtension()).toString();
    }


    /**
     * 构建qes字段映射对象
     * @param variable qes字段变量
     * @param questionnaireQes 问卷qes管理对象
     */
    private QesFieldMapping buildQesFieldMapping(String variable,QuestionnaireQes questionnaireQes){
        QesFieldMapping qesFieldMapping = new QesFieldMapping();
        qesFieldMapping.setQesId(questionnaireQes.getId());
        qesFieldMapping.setYear(questionnaireQes.getYear());
        String qesField = variable.replace(StrUtil.DELIM_START, StrUtil.EMPTY).replace(StrUtil.DELIM_END, StrUtil.EMPTY);
        qesFieldMapping.setQesField(qesField);
        return qesFieldMapping;
    }
}