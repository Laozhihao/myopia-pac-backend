package com.wupol.myopia.business.aggregation.export.pdf;

import cn.hutool.core.util.ZipUtil;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.business.management.export.domain.ExportCondition;
import com.wupol.myopia.business.management.export.interfaces.ExportFileService;
import com.wupol.myopia.business.management.service.NoticeService;
import com.wupol.myopia.business.management.util.S3Utils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Log4j2
@Service
public abstract class BaseExportFileService implements ExportFileService {

    @Value("${report.pdf.save-path}")
    public String pdfSavePath;

    @Autowired
    public NoticeService noticeService;
    @Autowired
    public S3Utils s3Utils;

    /**
     * 导出文件
     *
     * @param exportCondition 导出条件
     * @return void
     **/
    @Override
    public void export(ExportCondition exportCondition) {
        String fileName = getFileName(exportCondition);
        String parentPath = getFileSaveParentPath();
        String fileSavePath = getFileSavePath(parentPath, fileName);
        try {
            generateFile(exportCondition, fileSavePath, fileName);
            File file = compressFile(fileSavePath, fileName);
            Integer fileId = uploadFile(file);
            sendSuccessNotice(exportCondition.getApplyExportFileUserId(), fileName, fileId);
        } catch (Exception e) {
            log.error("【生成报告异常】{}", fileName, e);
            sendFailNotice(exportCondition.getApplyExportFileUserId(), fileName);
        } finally {
            deleteTempFile(parentPath);
        }
    }

    /**
     * 压缩文件
     *
     * @param fileSavePath 文件保存路径
     * @param fileName 文件名
     * @return java.io.File
     **/
    @Override
    public File compressFile(String fileSavePath, String fileName) {
        return ZipUtil.zip(fileSavePath);
    }

    /**
     * 上传文件
     *
     * @param zipFile 压缩文件
     * @return java.lang.Integer
     * @throws UtilException
     **/
    @Override
    public Integer uploadFile(File zipFile) throws UtilException {
        return s3Utils.uploadFileToS3(zipFile);
    }

    /**
     * 发送导出失败通知
     *
     * @param applyExportUserId 申请导出的用户ID
     * @param fileName 文件名
     * @param zipFileId 压缩文件ID
     * @return void
     **/
    @Override
    public void sendSuccessNotice(Integer applyExportUserId, String fileName, Integer zipFileId) {
        noticeService.sendExportSuccessNotice(applyExportUserId, applyExportUserId, fileName, zipFileId);
    }

    /**
     * 发送导出失败通知
     *
     * @param applyExportUserId 申请导出的用户ID
     * @param fileName 文件名
     * @return void
     **/
    @Override
    public void sendFailNotice(Integer applyExportUserId, String fileName) {
        noticeService.sendExportFailNotice(applyExportUserId, applyExportUserId, fileName);
    }

    /**
     * 删除临时文件
     *
     * @param directoryPath 文件所在目录路径
     * @return void
     **/
    @Override
    public void deleteTempFile(String directoryPath) {
        FileUtils.deleteQuietly(new File(directoryPath));
    }

    /**
     * 获取文件保存父目录路径
     *
     * @return java.lang.String
     **/
    @Override
    public String getFileSaveParentPath() {
        return Paths.get(pdfSavePath, UUID.randomUUID().toString()).toString();
    }

    /**
     * 获取文件保存路径
     *
     * @param parentPath 文件名
     * @param fileName 文件名
     * @return java.lang.String
     **/
    @Override
    public String getFileSavePath(String parentPath, String fileName) {
        return Paths.get(parentPath, fileName).toString();
    }
}
