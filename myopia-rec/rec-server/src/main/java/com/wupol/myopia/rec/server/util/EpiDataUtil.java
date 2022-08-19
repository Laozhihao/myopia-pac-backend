package com.wupol.myopia.rec.server.util;

import cn.hutool.core.io.FileUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.rec.server.exception.BusinessException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * EpiData数据出来工具
 *
 * @author hang.yuan 2022/7/19 10:38
 */
@Slf4j
@UtilityClass
public class EpiDataUtil {

    private static final String  GBK = "GBK";
    private static final String  EPIC = "/app/EpiC.exe";
    private static final String  TXT_TO_REC_MSG = "【导出REC文件异常】";

    /**
     * 获取根目录
     */
    public static String getRootPath() {
        String epiData = "EpiData/"+UUID.randomUUID().toString();
        String epiDataPath = IoUtil.getTempSubPath(epiData);
        File epiDataDirectory = new File(epiDataPath);
        if (!epiDataDirectory.exists()) {
            FileUtil.mkdir(epiDataDirectory);
        }
        return epiDataPath;
    }


    /**
     * 把数据导出成rec文件.
     * @param txtPath txt文件地址
     * @param qesPath qes文件地址
     * @param recPath rec文件地址
     */
    public static boolean exportRecFile(String txtPath, String qesPath, String recPath) {
        List<String> mainCmdList = Lists.newArrayList();
        if (!Objects.equals(IoUtil.windowsSystem(), Boolean.TRUE)) {
            mainCmdList.add("wine");
        }
        mainCmdList.add(EPIC);
        List<String> otherCmdList= Lists.newArrayList( "i", "TXT", txtPath, recPath, "qes="+qesPath, "delim=;", "q=text", "REPLACE", "ignorefirst", "date=yyyy/mm/dd" );
        mainCmdList.addAll(otherCmdList);
        String[] cmd = mainCmdList.toArray(new String[]{});

        log.info("[START]-[txt convert to rec] {}, {}", txtPath, recPath);
        log.info("execute command : {}", Arrays.toString(cmd));
        Process process;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
        } catch (IOException e) {
            log.error(TXT_TO_REC_MSG, e);
            return false;
        }

        // 获取命令的结果，判断是否成功
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("Error")) {
                    log.error(TXT_TO_REC_MSG+"：" + line);
                    process.destroy();
                    return false;
                }
            }
            return true;

        } catch (IOException e) {
            log.error(TXT_TO_REC_MSG, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }


}
