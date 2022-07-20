package com.wupol.myopia.business.api.questionnaire.service;

import com.wupol.myopia.business.core.common.util.S3Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * qes文件字段映射
 *
 * @author hang.yuan 2022/7/18 14:16
 */
@Component
public class QesFieldMappingFacade {

    @Autowired
    private S3Utils s3Utils;

    public void saveQes(MultipartFile file) {

    }
}
