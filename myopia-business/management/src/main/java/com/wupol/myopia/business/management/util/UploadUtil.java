package com.wupol.myopia.business.management.util;

import com.wupol.myopia.business.management.exception.FileTypeException;
import com.wupol.myopia.business.management.exception.UploadException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * 上传工具
 */
@Slf4j
public class UploadUtil {

    private UploadUtil() {
    }

    /**
     * 处理文件上传
     *
     * @param file 文件流
     * @param savePath 配置中存放文件的目录的绝对路径
     * @return TwoTuple(文件原始名称, 文件临时路径)
     */
    public static TwoTuple<String, String> upload(MultipartFile file, String savePath) {
        String orgFileName = file.getOriginalFilename();
        String imgUUID = UUID.randomUUID().toString();
        String fileName = imgUUID + "." + FilenameUtils.getExtension(orgFileName);
        try {
            File targetFile = new File(savePath, fileName);
            //把文件拷贝到服务器下面
            FileUtils.writeByteArrayToFile(targetFile, file.getBytes());
        } catch (IOException e) {
            log.error("文件保存到服务器失败:\n文件名: " + file.getOriginalFilename(), e);
            throw new UploadException("文件保存到服务器失败:\n文件名: " + file.getOriginalFilename(), e);
        }
        String path = savePath + "/" + fileName;
        return new TwoTuple<>(orgFileName, path);
    }
    public static void validateFileIsOverSize(MultipartFile file, Long sizeLimit) {
        if (file == null || file.getSize() == 0) {
            throw new UploadException("上传文件为空");
        }

        long fileSize = file.getSize();
        if(fileSize > sizeLimit) {
            throw new UploadException(String.format("文件超出限制大小, 限制为%d, 文件大小为%d", sizeLimit, fileSize));
        }
    }
    /**
     * 校验文件类型
     * @param file 文件流
     * @param fileExtensions 允许的后缀列表
     */
    public static void validateFileIsAllowed(MultipartFile file, String... fileExtensions) {
        if (file == null || file.getSize() == 0) {
            throw new UploadException("上传文件为空");
        }
        if(fileExtensions == null || fileExtensions.length == 0) {
            throw new FileTypeException("允许的扩展名为空");
        }
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        Optional<String> match = Arrays.stream(fileExtensions).filter(f -> f.equalsIgnoreCase(extension)).findAny();
        if(!match.isPresent()) {
            throw new FileTypeException("文件不允许上传");
        }
    }
}
