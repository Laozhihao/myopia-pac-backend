package com.wupol.myopia.business.common.utils.util;

import com.alibaba.excel.EasyExcel;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.IOUtils;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
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
        String fileName = IOUtils.getTempPath() + multipartFile.getName() + "_" + System.currentTimeMillis() + CommonConst.FILE_SUFFIX;
        File file = new File(fileName);
        try {
            org.apache.commons.io.FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
        } catch (IOException e) {
            log.error("导入数据异常:", e);
            throw new BusinessException("导入数据异常");
        }
        // 这里 也可以不指定class，返回一个list，然后读取第一个sheet 同步读取会自动finish
        try {
            List<Map<Integer, String>> listMap = EasyExcel.read(fileName).sheet().doReadSync();
            if (!CollectionUtils.isEmpty(listMap)) {
                listMap.remove(0);
            }
            return listMap;
        } catch (Exception e) {
            log.error("导入数据异常:", e);
            throw new BusinessException("Excel解析异常");
        }
    }

    /**
     * 文件下载
     *
     * @param fileUrl  下载路径
     * @param savePath 存放地址
     */
    public static void downloadFile(String fileUrl, String savePath) throws IOException {

        File file = new File(savePath);
        File parentFile = file.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            log.error("创建文件夹失败");
        }
        FileOutputStream fileOutputStream = null;
        try {
            if (!file.exists() && !file.createNewFile()) {
                log.error("创建文件失败");
            }

            URL url = new URL(fileUrl);
            URLConnection conn = url.openConnection();
            InputStream inputStream = conn.getInputStream();
            fileOutputStream = new FileOutputStream(savePath);
            int byteRead;
            byte[] buffer = new byte[1024];
            while ((byteRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
        } catch (IOException e) {
            throw new BusinessException("创建文件异常");
        } finally {
            if (Objects.nonNull(fileOutputStream)) {
                fileOutputStream.close();
            }
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
     * 递归删除目录下的所有文件及子目录下所有文件
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     *                 If a deletion fails, the method stops attempting to
     *                 delete and returns "false".
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i = 0; i< Objects.requireNonNull(children).length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

}
