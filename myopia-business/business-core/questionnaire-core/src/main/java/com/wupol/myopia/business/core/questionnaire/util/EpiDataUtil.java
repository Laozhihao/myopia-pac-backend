package com.wupol.myopia.business.core.questionnaire.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.IOUtils;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private static final String  EPIC = "/app/EpiC.exe";
    private static final String  TXT_TO_REC_MSG = "【TXT转REC异常】";

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
        String txtPath = Paths.get(epiDataPath,System.currentTimeMillis() + ".txt").toString();
        File epiDataDirectory = new File(epiDataPath);
        if (!epiDataDirectory.exists()){
            FileUtil.mkdir(epiDataDirectory);
        }

        // 先从数据转成txt文件，再转到rec文件
        boolean isSuccess = createTxt(headerList,dataList, txtPath);
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
     * 获取根目录
     */
    public static String getRootPath() {
        String epiData = QuestionnaireConstant.EPI_DATA_FOLDER+StrUtil.SLASH+UUID.randomUUID().toString();
        String epiDataPath = IOUtils.getTempSubPath(epiData);
        File epiDataDirectory = new File(epiDataPath);
        if (!epiDataDirectory.exists()) {
            FileUtil.mkdir(epiDataDirectory);
        }
        return epiDataPath;
    }

    /**
     * 获取生成txt文件路径
     * @param headerList rec文件的头模版的属性
     * @param dataList  需要导出的数据
     */
    public static String createTxtPath(List<String> headerList, List<List<String>> dataList){
        String epiDataPath = EpiDataUtil.getRootPath();
        String txtPath = Paths.get(epiDataPath,UUID.randomUUID().toString() + QuestionnaireConstant.TXT).toString();
        boolean isSuccess = createTxt(headerList, dataList, txtPath);
        if (isSuccess){
            return txtPath;
        }
        return StrUtil.EMPTY;
    }

    /**
     * 把内容写到指定的txt文件
     * @param headerList rec文件的头模版的属性
     * @param dataList  需要导出的数据
     * @param filePath 写入文件地址
     */
    private static boolean createTxt(List<String> headerList, List<List<String>> dataList, String filePath) {
        // 把两个List数据合并成txt所需要的指定的格式
        List<String> list = new ArrayList<>();
        list.add(String.join(";", headerList));
        dataList.forEach(itemList -> list.add(String.join(";", itemList)));

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
        List<String> mainCmdList = Lists.newArrayList();
        if (Objects.equals(windowsSystem(),Boolean.TRUE)){
            mainCmdList.add(EPIC);
        }else {
            mainCmdList.add("wine");
            mainCmdList.add(EPIC);
        }
        List<String> otherCmdList= Lists.newArrayList( "i", "TXT", txtPath, recPath, "qes="+qesPath, "delim=;", "q=text", "REPLACE", "ignorefirst", "date=dd/mm/yyyy" );
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

    /**
     * 判断是否windows系统
     */
    private static Boolean windowsSystem(){
        String system = System.getProperty("os.name").toLowerCase();
        return !Objects.equals("mac", system) && !Objects.equals("linux", system);
    }

}
