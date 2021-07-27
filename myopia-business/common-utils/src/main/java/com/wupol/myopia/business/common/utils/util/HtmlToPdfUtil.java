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
    private final String HTML_TO_PDF_TOOL_COMMAND = "wkhtmltopdf";
    /**
     * 失败重试次数
     **/
    public final int FAIL_RETRY_COUNT = 10;

    /**
     * 转换 TODO:超时处理
     *
     * @param htmlSrcPath html页面地址（可以是网页或者本地html文件绝对路径）
     * @param pdfFilePath 生成的PDF绝对路径
     * @return boolean
     **/
    public static boolean convert(String htmlSrcPath, String pdfFilePath) {
        return convert(htmlSrcPath, pdfFilePath, 0);
    }

    private static boolean convert(String htmlSrcPath, String pdfFilePath, int retryCount) {
        log.info("[START]-[html convert to pdf] {}, {}", htmlSrcPath, pdfFilePath);
        File file = new File(pdfFilePath);
        File parent = file.getParentFile();
        // 如果pdf保存路径不存在，则创建路径
        if(!parent.exists()){
            parent.mkdirs();
        }
        // "--window-status 1" 允许js异步请求
        ProcessBuilder processBuilder = new ProcessBuilder(HTML_TO_PDF_TOOL_COMMAND, "--load-media-error-handling", "ignore", "--load-error-handling", "ignore", "--javascript-delay", "2000", "--window-status", "1", htmlSrcPath, pdfFilePath);
        log.info(processBuilder.command().toString());
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
            if (exitCode == 0) {
                log.info("[SUCCESS]-[html convert to pdf] {}", pdfFilePath);
                return true;
            }
            // 如果转换失败则重试
            retryCount += 1;
            if (retryCount <= FAIL_RETRY_COUNT) {
                log.info("【转换失败重试】 第{}次，exitCode = {}", retryCount, exitCode);
                return convert(htmlSrcPath, pdfFilePath, retryCount);
            }
            log.error("[FAIL]-[html convert to pdf] {}", pdfFilePath);
            return false;
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
                log.error("【HTML转PDF异常】：关闭数据流异常", e);
            }
        }
    }

}