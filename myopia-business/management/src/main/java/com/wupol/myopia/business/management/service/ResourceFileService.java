package com.wupol.myopia.business.management.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.ResourceFileMapper;
import com.wupol.myopia.business.management.domain.model.ResourceFile;
import com.wupol.myopia.business.management.util.S3Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author Alix
 * @Date 2021-02-04
 */
@Service
@Slf4j
public class ResourceFileService extends BaseService<ResourceFileMapper, ResourceFile> {

    @Autowired
    private S3Utils s3Utils;


    /**
     * 根据文件Id获取路径
     * @param fileId
     * @return
     */
    public String getResourcePath(Integer fileId) {
        ResourceFile file = getById(fileId);
        if (Objects.isNull(file)) {
            return null;
        }
        return s3Utils.getResourcePath(file.getBucket(), file.getS3Key());
    }
}
