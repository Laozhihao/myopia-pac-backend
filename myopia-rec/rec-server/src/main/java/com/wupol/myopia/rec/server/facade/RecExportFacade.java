package com.wupol.myopia.rec.server.facade;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ZipUtil;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.rec.server.domain.TwoTuple;
import com.wupol.myopia.rec.server.domain.dto.RecExportDTO;
import com.wupol.myopia.rec.server.domain.vo.RecExportVO;
import com.wupol.myopia.rec.server.exception.BusinessException;
import com.wupol.myopia.rec.server.util.EpiDataUtil;
import com.wupol.myopia.rec.server.util.S3Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

/**
 * 导出REC门面
 *
 * @author hang.yuan 2022/8/9 16:20
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RecExportFacade {

    private final S3Util s3Util;

    private static final String QES = ".qes";
    private static final String TXT = ".txt";
    private static final String REC = ".rec";

    /**
     * REC文件导出
     * @param recExportDTO 导出条件
     */
    public RecExportVO recExport(RecExportDTO recExportDTO){
        RecExportVO recExportVO = new RecExportVO();

        String rootPath = EpiDataUtil.getRootPath();

        //rec文件地址
        TwoTuple<String, String> tuple = getRecSavePath(rootPath);

        // qes文件和txt文件下载
        String txtPath = getTxtPath(recExportDTO.getTxtUrl(),rootPath);
        String qesPath = getQesPath(recExportDTO.getQesUrl(),rootPath);

        //rec文件导出
        boolean exportRecFile = EpiDataUtil.exportRecFile(txtPath,qesPath, tuple.getSecond());
        if (Objects.equals(Boolean.FALSE,exportRecFile)){
            throw new BusinessException("generate rec file failed !");
        }
        log.info("[PROCESSING]-[generate rec file success]");
        recExportVO.setRecName(recExportDTO.getRecName());

        //不存在rec文件直接返回
        if (!FileUtil.exist(tuple.getSecond())){
            return recExportVO;
        }

        FileUtil.rename(FileUtil.newFile(tuple.getSecond()), recExportDTO.getRecName(), true, true);
        //上传rec文件到S3 获取S3链接
        try {
            File newFolder = FileUtil.rename(FileUtil.newFile(tuple.getFirst()), recExportDTO.getRecName(), true);
            File zipFile = compressFile(newFolder.getAbsolutePath());
            String recUrl = uploadFile(zipFile);
            recExportVO.setRecUrl(recUrl);
        } catch (Exception e) {
            throw new BusinessException("upload rec file failed !",e);
        }finally {
            FileUtil.del(rootPath);
        }
        log.info("[END]-[upload rec file success] {}",recExportVO.getRecUrl());
        return recExportVO;
    }

    /**
     * S3链接下载qes文件
     * @param qesUrl S3的qes文件链接
     */
    private String getQesPath(String qesUrl,String epiDataPath){
        String qesPath = Paths.get(epiDataPath,UUID.randomUUID().toString() + QES).toString();
        try {
            FileUtils.copyURLToFile(new URL(qesUrl), new File(qesPath));
        } catch (IOException e) {
            throw new BusinessException("create qes path failed, qesUrl="+qesUrl);
        }
        return qesPath;
    }

    /**
     * S3链接下载txt文件
     * @param txtUrl S3的txt文件链接
     */
    private String getTxtPath(String txtUrl,String epiDataPath){
        String txtPath = Paths.get(epiDataPath, UUID.randomUUID().toString() + TXT).toString();
        try {
            FileUtils.copyURLToFile(new URL(txtUrl), new File(txtPath));
        } catch (IOException e) {
            throw new BusinessException("create txt path failed, txtUrl="+txtUrl);
        }
        return txtPath;
    }

    /**
     * 获取REC文件父文件夹和REC文件路径
     */
    private TwoTuple<String,String> getRecSavePath(String rootPath){
        String recFolder = Paths.get(rootPath, UUID.randomUUID().toString()).toString();
        if(!FileUtil.exist(recFolder)){
            FileUtil.mkdir(recFolder);
        }
        String recPath = Paths.get(recFolder, UUID.randomUUID().toString() + REC).toString();
        return TwoTuple.of(recFolder,recPath);
    }

    /**
     * REC压缩
     * @param fileSavePath REC文件父文件夹
     */
    private File compressFile(String fileSavePath) {
        File srcFile = FileUtil.file(fileSavePath);
        final File zipFile = FileUtil.file(FileUtil.file(srcFile).getParentFile(), FileUtil.mainName(srcFile) + ".zip");
        // 将本目录也压缩
        return ZipUtil.zip(zipFile, CharsetUtil.defaultCharset(), true, srcFile);
    }

    /**
     * 上传REC压缩包 获取上传链接
     * @param zipFile REC压缩包
     */
    public String uploadFile(File zipFile) {
        try {
            String s3Key = s3Util.uploadFileToS3(zipFile, zipFile.getName());
            return s3Util.getResourcePathWithExpiredHours(s3Key);
        } catch (UtilException e) {
            throw new BusinessException("upload rec file failed !");
        }
    }
}
