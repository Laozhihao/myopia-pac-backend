package com.wupol.myopia.business.common.utils.util;

import com.alibaba.excel.EasyExcel;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.IOUtils;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

}
