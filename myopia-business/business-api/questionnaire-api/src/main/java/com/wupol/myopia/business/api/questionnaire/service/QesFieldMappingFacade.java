package com.wupol.myopia.business.api.questionnaire.service;

import cn.hutool.core.io.FileUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.questionnaire.domain.model.QesFieldMapping;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQes;
import com.wupol.myopia.business.core.questionnaire.service.QesFieldMappingService;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireQesService;
import com.wupol.myopia.business.core.questionnaire.util.EpiDataUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private final QuestionnaireQesService questionnaireQesService;
    private final QesFieldMappingService qesFieldMappingService;

    @Value("${file.temp.save-path}")
    public String fileSavePath;

    public void saveQesFieldMapping(Integer qesId) {
        QuestionnaireQes questionnaireQes = questionnaireQesService.getById(qesId);
        if (Objects.isNull(questionnaireQes.getQesFileId())){
            return;
        }
        String qesUrl = resourceFileService.getResourcePath(questionnaireQes.getQesFileId());
        List<String> variableList = Lists.newArrayList();
        String qesSavePath = getSavePath();
        try {
            FileUtils.copyURLToFile(new URL(qesUrl), new File(qesSavePath));
            EpiDataUtil.qesToVariable(qesSavePath,variableList);
        } catch (IOException e) {
            throw new BusinessException("生成区域报告PDF文件异常", e);
        }finally {
            FileUtil.del(qesSavePath);
        }

        if (CollectionUtils.isEmpty(variableList)){
            return;
        }
        List<QesFieldMapping> qesFieldMappingList = variableList.stream().map(variable -> buildQesFieldMapping(variable, questionnaireQes)).collect(Collectors.toList());
        qesFieldMappingService.saveBatch(qesFieldMappingList);

    }

    private String getSavePath(){
        return Paths.get(fileSavePath, UUID.randomUUID().toString()+".qes").toString();
    }


    private QesFieldMapping buildQesFieldMapping(String variable,QuestionnaireQes questionnaireQes){
        QesFieldMapping qesFieldMapping = new QesFieldMapping();
        qesFieldMapping.setQesId(questionnaireQes.getId());
        qesFieldMapping.setYear(questionnaireQes.getYear());
        String qesField = variable.replace("{", "").replace("}", "");
        qesFieldMapping.setQesField(qesField);
        return qesFieldMapping;
    }
}