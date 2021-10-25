package com.wupol.myopia.business.aggregation.export;

import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.business.aggregation.export.interfaces.ExportFileService;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.common.util.S3Utils;
import com.wupol.myopia.business.core.system.service.NoticeService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.File;

/**
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Log4j2
@Service
public abstract class BaseExportFileService implements ExportFileService {

    @Autowired
    public NoticeService noticeService;
    @Autowired
    public S3Utils s3Utils;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 导出前的校验
     *
     * @param exportCondition 导出条件
     * @return void
     **/
    @Override
    public void validateBeforeExport(ExportCondition exportCondition) {
        // 有需要校验的，重写覆盖该方法
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
     * 上锁
     *
     * @param key key
     * @return 是否成功
     */
    @Override
    public Boolean tryLock(String key) {
        return redisUtil.tryLock(key, "1", 60 * 20L);
    }

    /**
     * 释放锁
     *
     * @param key key
     */
    @Override
    public void unlock(String key) {
        Assert.isTrue(redisUtil.unlock(key), "Redis解锁异常,key=" + key);
    }
}
