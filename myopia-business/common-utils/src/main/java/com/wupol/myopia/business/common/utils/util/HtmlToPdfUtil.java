package com.wupol.myopia.business.common.utils.util;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * HTML转PDF工具
 *
 * @Author HaoHao
 * @Date 2021/3/17
 **/
@Log4j2
@UtilityClass
public class HtmlToPdfUtil {
    /**
     * 转换工具命令
     **/
    private static final String HTML_TO_PDF_TOOL_COMMAND = "wkhtmltopdf";

    /**
     * 转换 TODO:超时处理
     *
     * @param htmlSrcPath html页面地址（可以是网页或者本地html文件绝对路径）
     * @param pdfFilePath 生成的PDF绝对路径
     * @return boolean
     **/
    public static boolean convert(String htmlSrcPath, String pdfFilePath) {
        File file = new File(pdfFilePath);
        File parent = file.getParentFile();
        // 如果pdf保存路径不存在，则创建路径
        if(!parent.exists()){
            parent.mkdirs();
        }
        // "--window-status 1" 允许js异步请求
        ProcessBuilder processBuilder = new ProcessBuilder(HTML_TO_PDF_TOOL_COMMAND, "--window-status", "1", htmlSrcPath, pdfFilePath);
        log.debug(processBuilder.command().toString());
        processBuilder.redirectErrorStream(true);
        BufferedReader br = null;
        InputStreamReader reader = null;
        try {
            Process process = processBuilder.start();
            reader = new InputStreamReader(process.getInputStream());
            br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                log.debug(line);
                if (line.contains("Error")) {
                    log.error("【HTML转PDF异常】：" + line);
                    process.destroy();
                    return false;
                }
            }
            int exitCode = process.waitFor();
            log.debug("exitCode = "+exitCode);
        } catch (IOException | InterruptedException e) {
            log.error("【HTML转PDF异常】", e);
            Thread.currentThread().interrupt();
            return false;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                log.error("【HTML转PDF】关闭数据流异常", e);
            }
        }
        return true;
    }
}