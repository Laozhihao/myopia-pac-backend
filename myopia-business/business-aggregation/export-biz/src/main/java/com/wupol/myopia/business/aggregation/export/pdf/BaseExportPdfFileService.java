package com.wupol.myopia.business.aggregation.export.pdf;

import cn.hutool.core.util.ZipUtil;
import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.BaseExportFileService;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Log4j2
@Service
public abstract class BaseExportPdfFileService extends BaseExportFileService {

    @Value("${report.pdf.save-path}")
    public String pdfSavePath;

    @Autowired
    private ResourceFileService resourceFileService;

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
            File file = compressFile(fileSavePath);
            // 6.上传文件
            Integer fileId = uploadFile(file);
            // 7.发送成功通知
            sendSuccessNotice(exportCondition.getApplyExportFileUserId(), fileName, fileId);
            log.info("导出成功：{}", file.getName());
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
            // 9.释放锁
            unlock(getLockKey(exportCondition));
        }
    }

    /**
     * 生成文件
     *
     * @param exportCondition 导出条件
     * @param fileSavePath    文件保存路径
     * @param fileName        文件名
     * @return void
     **/
    public abstract void generatePdfFile(ExportCondition exportCondition, String fileSavePath, String fileName);

    /**
     * 压缩文件
     *
     * @param fileSavePath 文件保存路径
     * @return java.io.File
     **/
    public File compressFile(String fileSavePath) {
        return ZipUtil.zip(fileSavePath);
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
     * @param fileName   文件名
     * @return java.lang.String
     **/
    public String getFileSavePath(String parentPath, String fileName) {
        return Paths.get(parentPath, fileName).toString();
    }

    @Override
    public String syncExport(ExportCondition exportCondition) {
        String parentPath = null;
        try {
            // 1.获取文件名
            String fileName = getFileName(exportCondition);
            // 2.获取文件保存父目录路径
            parentPath = getFileSaveParentPath();
            // 3.获取文件保存路径
            String fileSavePath = getFileSavePath(parentPath, fileName);

            // 4.生成导出的文件
            generatePdfFile(exportCondition, fileSavePath, fileName);

            return resourceFileService.getResourcePath(s3Utils.uploadS3AndGetResourceFile(fileSavePath, fileName).getId());
        } catch (Exception e) {
            String requestData = JSON.toJSONString(exportCondition);
            log.error("【生成报告异常】{}", requestData, e);
            // 发送失败通知
            throw new BusinessException("导出数据异常");
        } finally {
            // 5.删除临时文件
            deleteTempFile(parentPath);
        }
    }
}
