package com.wupol.myopia.business.aggregation.export.pdf;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.alibaba.fastjson.JSON;
import com.vistel.framework.nodejs.pdf.domain.dto.response.PdfGenerateResponse;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.domain.vo.PDFRequestDTO;
import com.wupol.myopia.base.domain.vo.PdfGeneratorVO;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.BaseExportFileService;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.common.service.Html2PdfService;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Log4j2
@Service
public abstract class BaseExportPdfFileService extends BaseExportFileService {

    @Value("${file.temp.save-path}")
    public String pdfSavePath;

    @Autowired
    private ResourceFileService resourceFileService;

    @Resource
    private Html2PdfService html2PdfService;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 导出文件
     *
     * @param exportCondition 导出条件
     *
     * @return void
     **/
    @Async
    @Override
    public void export(ExportCondition exportCondition) {
        String fileName = null;
        String parentPath = null;
        try {
            // 0.前置处理
            preProcess(exportCondition);
            // 1.获取文件名(如果导出的是压缩包，这里文件名不带后缀，将作为压缩包的文件名)
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
            log.error("【生成PDF异常】{}", requestData, e);
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
     * 前置处理
     *
     * @param exportCondition 导出条件
     *
     * @return void
     **/
    public void preProcess(ExportCondition exportCondition) {
        // 有需要前置处理的，重写覆盖该方法
    }

    /**
     * 生成文件
     *
     * @param exportCondition 导出条件
     * @param fileSavePath    文件保存路径
     * @param fileName        文件名
     *
     * @return void
     **/
    public abstract void generatePdfFile(ExportCondition exportCondition, String fileSavePath, String fileName);

    /**
     * 压缩文件
     *
     * @param fileSavePath 文件保存路径
     *
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
     *
     * @return java.lang.String
     **/
    public String getFileSavePath(String parentPath, String fileName) {
        return Paths.get(parentPath, fileName).toString();
    }

    @Override
    public String syncExport(ExportCondition exportCondition) {
        String parentPath = null;
        try {
            // 0.前置处理
            preProcess(exportCondition);
            // 1.获取文件名(一般，同步导出的文件名带后缀，如：123.pdf)
            String fileName = getFileName(exportCondition);
            // 2.获取文件保存父目录路径
            parentPath = getFileSaveParentPath();
            // 3.获取文件保存路径
            String fileSavePath = getFileSavePath(parentPath, fileName);
            // 4.生成导出的文件
            generatePdfFile(exportCondition, fileSavePath, fileName);
            // 5.上传到S3
            return resourceFileService.getResourcePath(s3Utils.uploadS3AndGetResourceFile(fileSavePath, fileName).getId());
        } catch (Exception e) {
            String requestData = JSON.toJSONString(exportCondition);
            log.error("【生成PDF异常】{}", requestData, e);
            // 发送失败通知
            throw new BusinessException("导出数据异常");
        } finally {
            // 5.删除临时文件
            deleteTempFile(parentPath);
        }
    }

    @Override
    public void asyncGenerateExportFile(ExportCondition exportCondition) {
        preProcess(exportCondition);
        PDFRequestDTO pdfRequestDTO = getAsyncRequestUrl(exportCondition);
        List<PDFRequestDTO.Item> items = pdfRequestDTO.getItems();
        // 导出文件UUID
        String exportUuid = UUID.randomUUID().toString(true);
        // Redis Key
        String key = String.format(RedisConstant.FILE_EXPORT_ASYNC_TASK_KEY, exportUuid);

        PdfGeneratorVO pdfGenerator = new PdfGeneratorVO()
                .setUserId(exportCondition.getApplyExportFileUserId())
                .setExportTotal(items.size())
                .setExportCount(0)
                .setZipFileName(pdfRequestDTO.getZipFileName())
                .setLockKey(getLockKey(exportCondition))
                .setCreateTime(new Date())
                .setExportUuid(exportUuid);

        redisUtil.set(key, pdfGenerator);

        // 重试三次
        for (int i = 1; i <= 3; i++) {
            if (CollectionUtils.isEmpty(items)) {
                return;
            }
            items = requestHtml2Pdf(items, exportUuid);
        }
        if (CollectionUtils.isEmpty(items)) {
            log.error("生成PDF异常:{}", JSON.toJSONString(pdfRequestDTO));
            pdfGenerator.setStatus(Boolean.FALSE);
            redisUtil.set(key, pdfGenerator);
        }
    }

    /**
     * 发起请求
     *
     * @param items 项目
     * @param key   值
     *
     * @return List<PDFRequestDTO.Item>
     */
    private List<PDFRequestDTO.Item> requestHtml2Pdf(List<PDFRequestDTO.Item> items, String key) {
        return items.stream().map(item -> {
            PdfGenerateResponse pdfResponse = html2PdfService.asyncGeneratorPDF(item.getUrl(), org.apache.commons.lang3.StringUtils.substringAfterLast(item.getFileName(), StrUtil.SLASH), Paths.get(key, item.getFileName()).toString());
            if (Objects.equals(pdfResponse.getStatus(), Boolean.FALSE)) {
                return item;
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
