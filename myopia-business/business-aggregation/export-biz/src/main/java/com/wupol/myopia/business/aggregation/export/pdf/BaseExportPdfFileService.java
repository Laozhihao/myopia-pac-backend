package com.wupol.myopia.business.aggregation.export.pdf;

import cn.hutool.core.util.ZipUtil;
import com.alibaba.fastjson.JSON;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.business.aggregation.export.interfaces.ExportFileService;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.common.util.S3Utils;
import com.wupol.myopia.business.core.system.service.NoticeService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.UUID;

/**
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Log4j2
@Service
public abstract class BaseExportPdfFileService implements ExportFileService {

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
    @Async
    @Override
    public void export(ExportCondition exportCondition) {
        String fileName = null;
        String parentPath = null;
        try {
            // 1.获取文件名
            fileName = getFileName(exportCondition);
            // 2.获取文件保存父目录路径
            parentPath = getFileSaveParentPath();
            // 3.获取文件保存路径
            String fileSavePath = getFileSavePath(parentPath, fileName);
            // 4.生成导出的文件
            generatePdfFile(exportCondition, fileSavePath, fileName);
            // 5.压缩文件
            File file = compressFile(fileSavePath, fileName);
            // 6.上传文件
            Integer fileId = uploadFile(file);
            // 7.发送成功通知
            sendSuccessNotice(exportCondition.getApplyExportFileUserId(), fileName, fileId);
        } catch (Exception e) {
            String requestData = JSON.toJSONString(exportCondition);
            log.error("【生成报告异常】{}", requestData, e);
            // 发送失败通知
            if (!StringUtils.isEmpty(fileName)) {
                sendFailNotice(exportCondition.getApplyExportFileUserId(), fileName);
            }
        } finally {
            // 8.删除临时文件
            deleteTempFile(parentPath);
        }
    }

    /**
     * 导出前的校验
     *
     * @param exportCondition 导出条件
     * @return void
     **/
    @Override
    public void validateBeforeExport(ExportCondition exportCondition) throws IOException {
        // 有需要校验的，重写覆盖该方法
    }

    /**
     * 生成文件
     *
     * @param exportCondition 导出条件
     * @param fileSavePath 文件保存路径
     * @param fileName 文件名
     * @return void
     **/
    public abstract void generatePdfFile(ExportCondition exportCondition, String fileSavePath, String fileName);

    /**
     * 压缩文件
     *
     * @param fileSavePath 文件保存路径
     * @param fileName 文件名
     * @return java.io.File
     **/
    public File compressFile(String fileSavePath, String fileName) {
        String[] ext = {"pdf"};
        Collection<File> files = FileUtils.listFiles(new File(fileSavePath), ext, false);
        files.forEach(x -> log.info(x.getName()));
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
        if (StringUtils.isEmpty(directoryPath)) {
            return;
        }
        FileUtils.deleteQuietly(new File(directoryPath));
    }

    /**
     * 获取文件保存父目录路径
     *
     * @return java.lang.String
     **/
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
    public String getFileSavePath(String parentPath, String fileName) {
        return Paths.get(parentPath, fileName).toString();
    }
}
