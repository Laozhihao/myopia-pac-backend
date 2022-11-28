package com.wupol.myopia.business.common.utils.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.IOUtils;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
public final class FileUtils {

    private FileUtils() {

    }

    /**
     * 读取Excel数据
     *
     * @param multipartFile Excel文件
     * @return java.util.List<java.util.Map < java.lang.Integer, java.lang.String>>
     **/
    public static List<Map<Integer, String>> readExcel(MultipartFile multipartFile) {
        String tempPath = IOUtils.getTempPath();
        String suffix = Objects.requireNonNull(multipartFile.getOriginalFilename()).substring(multipartFile.getOriginalFilename().lastIndexOf("."));
        String fileName = StringUtils.EMPTY;
        if (StringUtils.equals(suffix, CommonConst.EXCEL_XLSX_FILE_SUFFIX)) {
            fileName = multipartFile.getName() + StrUtil.UNDERLINE + System.currentTimeMillis() + CommonConst.EXCEL_XLSX_FILE_SUFFIX;
        }
        if (StringUtils.equals(suffix, CommonConst.EXCEL_XLS_FILE_SUFFIX)) {
            fileName = multipartFile.getName() + StrUtil.UNDERLINE + System.currentTimeMillis() + CommonConst.EXCEL_XLS_FILE_SUFFIX;
        }
        if (StringUtils.isBlank(fileName)) {
            throw new BusinessException("文件格式有误!");
        }
        fileName = Paths.get(tempPath, fileName).toString();
        File file = new File(fileName);
        try {
            org.apache.commons.io.FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
        } catch (IOException e) {
            log.error("导入数据异常:", e);
            throw new BusinessException("导入数据异常");
        }
        // 这里 也可以不指定class，返回一个list，然后读取第一个sheet 同步读取会自动finish
        try {
            List<Map<Integer, String>> listMap = EasyExcelFactory.read(fileName).sheet().doReadSync();
            if (!CollectionUtils.isEmpty(listMap)) {
                listMap.remove(0);
            }
            return listMap;
        } catch (Exception e) {
            log.error("导入数据异常:", e);
            throw new BusinessException("Excel解析异常");
        } finally {
            FileUtil.del(file);
        }
    }

    /**
     * 文件下载
     *
     * @param fileUrl  下载路径
     * @param savePath 存放地址
     */
    public static void downloadFile(String fileUrl, String savePath) {

        File file = new File(savePath);
        File parentFile = file.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            log.error("创建文件夹失败");
            throw new BusinessException("创建文件失败");
        }

        try (FileOutputStream fos = new FileOutputStream(savePath)) {

            if (!file.exists() && !file.createNewFile()) {
                log.error("创建文件失败");
                throw new BusinessException("创建文件失败");
            }
            URL url = new URL(fileUrl);
            InputStream inputStream = url.openConnection().getInputStream();
            int byteRead;
            byte[] buffer = new byte[1024];
            while ((byteRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, byteRead);
            }
        } catch (IOException e) {
            log.error("创建文件异常，请求URL:{}, 保存地址:{}", fileUrl, savePath, e);
            throw new BusinessException("创建文件异常");
        }
    }

    /**
     * 获取文件保存父目录路径
     *
     * @return java.lang.String
     **/
    public static String getFileSaveParentPath(String savePath) {
        return Paths.get(savePath, UUID.randomUUID().toString()).toString();
    }

    /**
     * 获取文件保存路径
     *
     * @param parentPath 文件父路径
     * @param fileName   文件名
     * @return 文件保存路径
     **/
    public static String getFileSavePath(String parentPath, String fileName) {
        return Paths.get(parentPath, fileName).toString();
    }

    /**
     * 是否包含文件
     * @param srcFile 父文件或父文件夹
     */
    public static Boolean includeFiles(File srcFile){
        if (Objects.isNull(srcFile)){
            return Boolean.FALSE;
        }
        if (srcFile.isFile()) {
            return Boolean.TRUE;
        }
        List<File> fileList = Lists.newArrayList();
        getFileList(srcFile,fileList);
        return !CollectionUtils.isEmpty(fileList);
    }

    /**
     * 获取文件递归
     * @param srcFile 父文件或父文件夹
     */
    public static void getFileList(File srcFile,List<File> fileList) {
        if (srcFile.isFile()){
            fileList.add(srcFile);
        }
        File[] files = srcFile.listFiles();
        if (ArrayUtil.isEmpty(files)) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()){
                getFileList(file,fileList);
            }else {
                fileList.add(file);
            }
        }
    }
}
