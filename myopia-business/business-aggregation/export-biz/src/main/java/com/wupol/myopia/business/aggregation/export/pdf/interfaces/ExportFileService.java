package com.wupol.myopia.business.aggregation.export.pdf.interfaces;

import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;

import java.io.File;

/**
 * 导出文件接口类
 *
 * @Author HaoHao
 * @Date 2021/3/24
 **/
public interface ExportFileService {

    /**
     * 导出文件
     *
     * @param exportCondition 导出条件
     * @return void
     **/
    void export(ExportCondition exportCondition);

    /**
     * 生成文件
     *
     * @param exportCondition 导出条件
     * @param fileSavePath 文件保存路径
     * @param fileName 文件名
     * @return void
     **/
    void generateFile(ExportCondition exportCondition, String fileSavePath, String fileName);

    /**
     * 压缩文件
     *
     * @param fileSavePath 文件保存路径
     * @param fileName 文件名
     * @return java.io.File
     **/
    File compressFile(String fileSavePath, String fileName);

    /**
     * 上传文件
     *
     * @param zipFile 压缩文件
     * @return java.lang.Integer
     * @throws UtilException
     **/
    Integer uploadFile(File zipFile) throws UtilException;

    /**
     * 发送导出失败通知
     *
     * @param applyExportUserId 申请导出的用户ID
     * @param fileName 文件名
     * @param zipFileId 压缩文件ID
     * @return void
     **/
    void sendSuccessNotice(Integer applyExportUserId, String fileName, Integer zipFileId);

    /**
     * 发送导出失败通知
     *
     * @param applyExportUserId 申请导出的用户ID
     * @param fileName 文件名
     * @return void
     **/
    void sendFailNotice(Integer applyExportUserId, String fileName);

    /**
     * 删除临时文件
     *
     * @param directoryPath 文件所在目录路径
     * @return void
     **/
    void deleteTempFile(String directoryPath);

    /**
     * 获取文件名
     *
     * @param exportCondition 导出条件
     * @return java.lang.String
     **/
    String getFileName(ExportCondition exportCondition);

    /**
     * 获取文件保存父目录路径
     *
     * @return java.lang.String
     **/
    String getFileSaveParentPath();

    /**
     * 获取文件保存路径
     *
     * @param parentPath 文件名
     * @param fileName 文件名
     * @return java.lang.String
     **/
    String getFileSavePath(String parentPath, String fileName);
}
