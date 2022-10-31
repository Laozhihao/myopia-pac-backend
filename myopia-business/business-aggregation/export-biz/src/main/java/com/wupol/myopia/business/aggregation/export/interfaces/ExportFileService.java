package com.wupol.myopia.business.aggregation.export.interfaces;

import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.domain.vo.PDFRequestDTO;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 导出文件接口类
 *
 * @Author HaoHao
 * @Date 2021/3/24
 **/
public interface ExportFileService {

    /**
     * 导出前的校验
     *
     * @param exportCondition 导出条件
     * @return void
     * @throws IOException
     **/
    void validateBeforeExport(ExportCondition exportCondition) throws IOException;

    /**
     * 导出文件
     *
     * @param exportCondition 导出条件
     * @return void
     **/
    void export(ExportCondition exportCondition);

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
     * @param fileName          文件名
     * @param zipFileId         压缩文件ID
     * @return void
     **/
    void sendSuccessNotice(Integer applyExportUserId, String fileName, Integer zipFileId);

    /**
     * 发送导出失败通知
     *
     * @param applyExportUserId 申请导出的用户ID
     * @param fileName          文件名
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
     * 1.同步导出，带后缀，作为导出文件的文件名
     * 2.异步导出，不带后缀，作为压缩包zip文件名，并作为站内消息的关键内容
     *
     * @param exportCondition 导出条件
     * @return java.lang.String
     **/
    String getFileName(ExportCondition exportCondition);

    /**
     * 获取Key名字
     *
     * @param exportCondition 导出条件
     * @return Key
     */
    String getLockKey(ExportCondition exportCondition);

    /**
     * 上锁
     *
     * @param key key
     * @return 是否成功
     */
    Boolean tryLock(String key);

    /**
     * 解锁
     *
     * @param key key
     */
    void unlock(String key);

    /**
     * 同步导出文件
     *
     * @param exportCondition 条件
     * @return file
     */
    String syncExport(ExportCondition exportCondition);

    default void asyncExportUrl(ExportCondition exportCondition){}

    default PDFRequestDTO allUrl(ExportCondition exportCondition){
        return new PDFRequestDTO();
    }
}
