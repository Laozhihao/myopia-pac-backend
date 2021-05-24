package com.wupol.myopia.business.common.utils.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

@Slf4j
public final class FileUtils {

    private FileUtils() {

    }

    /** 根据文件设置返回到前端的文件流,用于前端下载 */
    public static ResponseEntity<FileSystemResource> getResponseEntity(File file) {
        if (file == null) {
            return null;
        }
        String fileName = file.getName();
        try {
            fileName = URLEncoder.encode(file.getName(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.warn("转换文件名失败", e);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", "attachment; filename=" + fileName);
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Last-Modified", new Date().toString());
        headers.add("ETag", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok().headers(headers).contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(file));
    }

}
