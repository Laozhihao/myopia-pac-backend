package com.wupol.myopia.business.core.questionnaire.util;

import cn.hutool.core.io.FileUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.IOUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EpiData数据出来工具
 *
 * @author hang.yuan 2022/7/19 10:38
 */
@Slf4j
@UtilityClass
public class EpiDataUtil {

    private static final String  GBK = "GBK";
    private static final String  PARAMETER = "\\{([^}]*)\\}";
    private static final String  EPIC = "EpiC.exe";

    /**
     * qes文件解析为txt文件
     *
     * @param qesFilePath qes文件地址
     * @param txtFilePath txt文件地址
     */
    public static void qesToTxt(String qesFilePath, String txtFilePath){
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(qesFilePath),GBK));
             BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(txtFilePath), GBK))) {
            String line;
            while ((line = br.readLine()) != null) {
                bw.write(line);
                bw.newLine();
            }
        }catch (IOException e){
            log.error("【QES转TXT异常】", e);
            Thread.currentThread().interrupt();
            throw new BusinessException("QES文件解析异常");
        }
    }

    /**
     * qes文件解析变量
     *
     * @param qesFilePath qes文件地址
     * @param variableList qes文件中变量集合
     */
    public static void qesToVariable(String qesFilePath, List<String> variableList){
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(qesFilePath),GBK))) {
            String line;
            while ((line = br.readLine()) != null) {
                regularMatch(line,variableList);
            }
        }catch (IOException e){
            log.error("【QES解析变量异常】", e);
            Thread.currentThread().interrupt();
            throw new BusinessException("QES解析变量异常");
        }

    }

    /**
     * 正则匹配：获取指定字符串内大括号的内容
     * @param str 匹配字符串
     * @param variableList 匹配结果集合
     */
    public static void regularMatch(String str,List<String> variableList){
        Pattern compile = Pattern.compile(PARAMETER);
        Matcher matcher = compile.matcher(str);
        while (matcher.find()) {
            String group = matcher.group();
            variableList.add(group);
        }
    }


    /**
     * 把数据导出成rec文件.
     * @param qesPath       qes文件的路径，用于与txt合并生成带数据的rec文件
     * @param recPath       rec文件的路径
     * @param headerList    rec文件的头模版的属性
     * @param dataList      需要导出的数据
     */
    public static boolean exportRecFile(String qesPath, String recPath, List<String> headerList, List<List<String>> dataList) {
        String epiDataPath = IOUtils.getTempSubPath("EpiData");
        String txtPath = epiDataPath + System.currentTimeMillis() + ".txt";
        File epiDataDirectory = new File(epiDataPath);
        if (!epiDataDirectory.exists()){
            FileUtil.mkdir(epiDataDirectory);
        }

        // 把两个List数据合并成txt所需要的指定的格式
        List<String> list = new ArrayList<>();
        list.add(String.join(";", headerList));
        dataList.forEach(itemList -> list.add(String.join(";", itemList)));

        // 先从数据转成txt文件，再转到rec文件
        boolean isSuccess;
        isSuccess = createTxt(list, txtPath);
        if (!isSuccess) {
            return false;
        }
        isSuccess = txt2Rec(txtPath, qesPath, recPath);
        // 生成成功，删除txt文件
        if (isSuccess) {
            FileUtil.del(txtPath);
        }
        return isSuccess;
    }


    /**
     * 把内容写到指定的txt文件
     * @param list 写入内容集合
     * @param filePath 写入文件地址
     */
    private static boolean createTxt(List<String> list, String filePath) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), GBK))) {
            for (String data : list) {
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
     * txt文件转rec文件
     * @param txtPath txt文件地址
     * @param qesPath qes文件地址
     * @param recPath rec文件地址
     */
    private static boolean txt2Rec(String txtPath, String qesPath, String recPath) {
        String[] cmd = { EPIC, "i", "TXT", txtPath, recPath, "qes="+qesPath, "delim=;", "q=text", "REPLACE", "ignorefirst", "date=dd/mm/yyyy" };
        log.info("[START]-[txt convert to rec] {}, {}", txtPath, recPath);
        Process process;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
        } catch (IOException e) {
            log.error("【TXT转REC异常】", e);
            return false;
        }

        // 获取命令的结果，判断是否成功
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("Error")) {
                    log.error("【TXT转REC异常】：" + line);
                    process.destroy();
                    return false;
                }
            }
            return true;

        } catch (IOException e) {
            log.error("【TXT转REC异常】", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
