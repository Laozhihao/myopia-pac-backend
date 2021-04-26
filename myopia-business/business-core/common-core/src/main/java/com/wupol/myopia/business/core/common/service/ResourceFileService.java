package com.wupol.myopia.business.core.common.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.common.domain.mapper.ResourceFileMapper;
import com.wupol.myopia.business.core.common.domain.model.ResourceFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    /**
     * 根据文件Id获取路径
     * @param fileIdList
     * @return
     */
    public List<String> getBatchResourcePath(List<Integer> fileIdList) {
        return fileIdList.stream().map(this::getResourcePath).collect(Collectors.toList());
    }

}
