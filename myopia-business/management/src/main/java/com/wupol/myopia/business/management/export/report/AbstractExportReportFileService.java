package com.wupol.myopia.business.management.export.report;

import cn.hutool.core.util.ZipUtil;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.export.domain.ExportCondition;
import com.wupol.myopia.business.management.export.interfaces.ExportFileService;
import com.wupol.myopia.business.management.service.NoticeService;
import com.wupol.myopia.business.management.service.SchoolService;
import com.wupol.myopia.business.management.util.HtmlToPdfUtil;
import com.wupol.myopia.business.management.util.S3Utils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Log4j2
@Service
public abstract class AbstractExportReportFileService implements ExportFileService {

    /** 学校报告HTML页面地址，带筛查通知ID参数 **/
    private static final String SCHOOL_REPORT_HTML_PATH_WITH_NOTICE_ID = "%s?notificationId=%d&schoolId=%d";
    /** 学校报告HTML页面地址，带筛查计划ID参数 **/
    private static final String SCHOOL_REPORT_HTML_PATH_WITH_PLAN_ID = "%s?planId=%d&schoolId=%d";
    /** PDF报告文件名 **/
    static final String PDF_REPORT_FILE_NAME = "%s筛查报告";

    @Value("${report.html.url-host}")
    public String htmlUrlHost;
    @Value("${report.pdf.save-path}")
    public String pdfSavePath;

    @Autowired
    NoticeService noticeService;
    @Autowired
    S3Utils s3Utils;
    @Autowired
    SchoolService schoolService;

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
        return s3Utils.uploadS3AndGetResourceFile(zipFile.getAbsolutePath(), UUID.randomUUID().toString() + "_" + zipFile.getName()).getId();
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

    /**
     * 批量生成学校筛查报告PDF文件
     *
     * @param saveDirectory 文件保存目录
     * @param noticeId 筛查通知ID
     * @param planId 筛查计划ID
     * @param schoolIdList 学校ID集合
     * @return void
     **/
    void generateSchoolPdfFileBatch(String saveDirectory, Integer noticeId, Integer planId, List<Integer> schoolIdList) {
        schoolIdList.forEach(schoolId -> generateSchoolPdfFile(saveDirectory, noticeId, planId, schoolId));
    }

    /**
     * 生成学校筛查报告PDF文件
     *
     * @param saveDirectory 文件保存目录
     * @param noticeId 筛查通知ID
     * @param planId 筛查计划ID
     * @param schoolId 学校ID
     * @return void
     **/
    void generateSchoolPdfFile(String saveDirectory, Integer noticeId, Integer planId, Integer schoolId) {
        School school = schoolService.getById(schoolId);
        String schoolReportFileName = String.format(PDF_REPORT_FILE_NAME, school.getName());
        String schoolPdfHtmlUrl = String.format(Objects.isNull(noticeId) ? SCHOOL_REPORT_HTML_PATH_WITH_PLAN_ID : SCHOOL_REPORT_HTML_PATH_WITH_NOTICE_ID, htmlUrlHost, Objects.isNull(noticeId) ? planId : noticeId, schoolId);
        Assert.isTrue(HtmlToPdfUtil.convert(schoolPdfHtmlUrl, Paths.get(saveDirectory, schoolReportFileName + ".pdf").toString()), "【生成区域报告异常】：" + school.getName());
    }
}
