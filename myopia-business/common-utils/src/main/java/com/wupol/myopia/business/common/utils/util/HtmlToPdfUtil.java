package com.wupol.myopia.business.common.utils.util;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.Assert;

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
    private final int FAIL_RETRY_COUNT = 10;

    /**
     * 转换（通用版）
     *
     * @param htmlSrcPath html页面地址（可以是网页或者本地html文件绝对路径）
     * @param pdfFilePath 生成的PDF绝对路径
     * @return boolean
     **/
    public static boolean convert(String htmlSrcPath, String pdfFilePath) {
        // "--window-status 1" 允许js异步请求
        String[] command = {HTML_TO_PDF_TOOL_COMMAND, "--debug-javascript", "--no-stop-slow-scripts", "--window-status", "1", htmlSrcPath, pdfFilePath};
        return convert(htmlSrcPath, pdfFilePath, 0, command);
    }

    /**
     * 转换（档案卡专用）
     *
     * @param htmlSrcPath html页面地址（可以是网页或者本地html文件绝对路径）
     * @param pdfFilePath 生成的PDF绝对路径
     * @return boolean
     **/
    public static boolean convertArchives(String htmlSrcPath, String pdfFilePath) {
        // "--window-status 1" 允许js异步请求
        String[] command = {HTML_TO_PDF_TOOL_COMMAND, "-L", "0", "-R", "0", "-T", "0", "-B", "0", "--disable-smart-shrinking", "--debug-javascript", "--no-stop-slow-scripts", "--window-status", "1", htmlSrcPath, pdfFilePath};
        return convert(htmlSrcPath, pdfFilePath, 0, command);
    }

    private static boolean convert(String htmlSrcPath, String pdfFilePath, int retryCount, String[] command) {
        log.info("[START]-[html convert to pdf] {}, {}", htmlSrcPath, pdfFilePath);
        mkdir(pdfFilePath);
        Process process;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
        } catch (IOException e) {
            log.error("【HTML转PDF异常】", e);
            return false;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("Error")) {
                    log.error("【HTML转PDF异常】：" + line);
                    process.destroy();
                    return false;
                }
            }
            int exitCode = process.waitFor();
            return checkResult(htmlSrcPath, pdfFilePath, retryCount, exitCode, command);
        } catch (IOException | InterruptedException e) {
            log.error("【HTML转PDF异常】", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 生成文件夹
     *
     * @param pdfFilePath PDF文件路径
     * @return void
     **/
    private void mkdir(String pdfFilePath) {
        File file = new File(pdfFilePath);
        File parent = file.getParentFile();
        // 如果pdf保存路径不存在，则创建路径
        if(!parent.exists()){
            Assert.isTrue(parent.mkdirs(), "创建PDF路径失败");
        }
    }

    /**
     * 检查生成结果
     *
     * @param htmlSrcPath html页面访问路径
     * @param pdfFilePath PDF文件路径
     * @param retryCount 当前尝试次数
     * @param exitCode 命令执行完成状态码
     * @param command 命令
     * @return boolean
     **/
    private boolean checkResult(String htmlSrcPath, String pdfFilePath, int retryCount, int exitCode, String[] command) {
        if (exitCode == 0) {
            return true;
        }
        // 如果转换失败则重试
        retryCount += 1;
        if (retryCount <= FAIL_RETRY_COUNT) {
            log.info("【转换失败重试】 第{}次，exitCode = {}", retryCount, exitCode);
            return convert(htmlSrcPath, pdfFilePath, retryCount, command);
        }
        log.error("[FAIL]-[html convert to pdf] {}", pdfFilePath);
        return false;
    }

}