package com.wupol.myopia.rec.server.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
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
    private static final String  EPIC = "/wine/EpiC.exe";
    private static final String  TXT_TO_REC_MSG = "【导出REC文件异常】";
    private static final String  EPI_DATA_FOLDER = "EpiData";

    /**
     * 获取根目录
     */
    public static String getRootPath() {
        String epiData = EPI_DATA_FOLDER+StrUtil.SLASH+UUID.randomUUID().toString();
        String epiDataPath = IoUtil.getTempSubPath(epiData);
        File epiDataDirectory = new File(epiDataPath);
        if (!epiDataDirectory.exists()) {
            FileUtil.mkdir(epiDataDirectory);
        }
        return epiDataPath;
    }


    /**
     * 把内容写到指定的txt文件
     * @param dataList  需要导出的数据
     * @param filePath 写入文件地址
     */
    public static boolean createTxt(List<String> dataList, String filePath) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), GBK))) {
            for (String data : dataList) {
                bw.write(data);
                bw.newLine();
            }
            return true;
        } catch (Exception e) {
            log.error("生成txt文件失败", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }


    /**
     * 把数据导出成rec文件.
     * @param txtPath txt文件地址
     * @param qesPath qes文件地址
     * @param recPath rec文件地址
     */
    public static boolean exportRecFile(String txtPath, String qesPath, String recPath) {
        String[] cmd = buildCmd(txtPath, qesPath, recPath);
        log.info("[START]-[txt convert to rec] ,execute command : {}", Arrays.toString(cmd));
        return execCmd(cmd,Boolean.FALSE);
    }

    /**
     * 执行命令
     * @param cmd 命令
     * @param isPrint 是否打印
     */
    private static boolean execCmd(String[] cmd,Boolean isPrint) {
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
                if (Objects.equals(isPrint,Boolean.TRUE)){
                    log.info(line);
                }
            }
            return true;

        } catch (IOException e) {
            log.error(TXT_TO_REC_MSG, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 构建执行命令
     * @param txtPath txt文件路径
     * @param qesPath qes文件路径
     * @param recPath rec文件路径
     */
    private static String[] buildCmd(String txtPath, String qesPath, String recPath) {
        List<String> mainCmdList = Lists.newArrayList();
        if (Objects.equals(IoUtil.windowsSystem(), Boolean.FALSE)) {
            mainCmdList.add("wine");
        }
        mainCmdList.add(EPIC);
        List<String> otherCmdList= Lists.newArrayList( "i", "TXT", txtPath, recPath, "qes="+qesPath, "delim=;", "q=text", "REPLACE", "ignorefirst", "date=yyyy/mm/dd" );
        mainCmdList.addAll(otherCmdList);
        return mainCmdList.toArray(new String[]{});
    }

    /**
     * 初始化Epic程序
     */
    public static Boolean initEpic(){
        List<String> mainCmdList = Lists.newArrayList();
        if (Objects.equals(IoUtil.windowsSystem(), Boolean.FALSE)) {
            mainCmdList.add("wine");
        }
        mainCmdList.add(EPIC);
        String[] cmd = mainCmdList.toArray(new String[]{});
        log.info("[START]-[initialized EpiC config] ,execute command : {}", Arrays.toString(cmd));
        return execCmd(cmd,Boolean.TRUE);
    }

}
