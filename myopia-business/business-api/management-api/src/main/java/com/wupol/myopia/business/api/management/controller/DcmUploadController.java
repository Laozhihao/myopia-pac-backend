package com.wupol.myopia.business.api.management.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.alibaba.fastjson.JSON;
import com.vistel.Interface.exception.UtilException;
import com.vistel.Interface.util.ReturnInformation;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.business.common.utils.config.UploadConfig;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.common.utils.util.UploadUtil;
import com.wupol.myopia.business.core.common.domain.model.ResourceFile;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.common.util.S3Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * TODO:
 *
 * @author Simple4H
 */
@CrossOrigin
@RequestMapping("/management/device")
@RestController
@Slf4j
public class DcmUploadController {

    @Autowired
    private UploadConfig uploadConfig;

    @Resource
    private ResourceFileService resourceFileService;

    @Resource
    private S3Utils s3Utils;


    @PostMapping(value = "/jsonV2", consumes = {"multipart/form-data"})
    public String addV2(DeviceRequestDTO requestDTO) throws UtilException {
        List<DicomDTO> dicomDTOS = JSON.parseArray(requestDTO.getJson(), DicomDTO.class);
        log.info(JSON.toJSONString(dicomDTOS));
        TwoTuple<String, String> upload = UploadUtil.upload(requestDTO.getPic(), uploadConfig.getSavePath());
        String path = upload.getSecond();
        ZipUtil.unzip(path);

        String s = StrUtil.removeSuffix(path, ".zip");
        File file = new File(s);
        File[] files = file.listFiles();
        if (Objects.isNull(files)) {
            return null;
        }

        // 获取后缀威jpg的图像文件
        List<File> fileList = new ArrayList<>();
        for (File f : files) {
            if (f.getName().endsWith(".jpg") || f.getName().endsWith(".pdf")) {
                fileList.add(f);
            }
        }

        if (CollectionUtils.isEmpty(fileList)) {
            return ReturnInformation.returnSuccess();
        }

        // 上传原始文件
        ResourceFile sourceFileZip = resourceFileService.uploadFileAndSave(requestDTO.getPic());


        // 上传图片信息
        for (File imageFile : fileList) {
            ResourceFile resourceFile = s3Utils.uploadS3AndGetResourceFileAndDeleteTempFile(imageFile, imageFile.getName());
        }


        // 读取JSON文件信息
        FileReader fileReader = new FileReader(StrUtil.removeSuffix(path, ".zip") + "/" + dicomDTOS.get(0).getBase());
        String dicomJson = fileReader.readString();

        log.info("result:{}", dicomJson);
        // 删除临时文件
        deletedFile(path);
        return ReturnInformation.returnSuccess();
    }

    @GetMapping("resource")
    public ApiResult<String> getResource(Integer id) {
        return ApiResult.success(resourceFileService.getResourcePath(id));
    }

    private void deletedFile(String path) {
        String s = StrUtil.removeSuffix(path, ".zip");
        // 删除文件
        FileUtil.del(path);
        // 删除文件夹
        FileUtil.del(s);
    }

    @Getter
    @Setter
    static class DeviceRequestDTO {
        private String json;
        private String name;
        // 大小
        private long length;
        // 文件 zip压缩包
        private MultipartFile pic;
    }

    @Getter
    @Setter
    static class DicomDTO {
        private String fileName;
        private String macAddress;
        private String dcmName;
        // 判断是否重复上传
        private String md5;
        // 基本信息-JSON文件名字
        private String base;
        // 0-图片 1-pdf
        private Integer fileType;
        private Integer aaid;
    }
}
