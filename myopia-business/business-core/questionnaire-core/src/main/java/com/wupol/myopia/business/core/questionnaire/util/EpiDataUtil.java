package com.wupol.myopia.business.core.questionnaire.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.IOUtils;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QesFieldDataBO;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
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
    private static final String  EPIC = "/wine/EpiC.exe";
    private static final String  TXT_TO_REC_MSG = "【TXT转REC异常】";
    private static final String  SEMICOLON = ";";

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
        String epiDataPath = IOUtils.getTempSubPath(QuestionnaireConstant.EPI_DATA_FOLDER);
        String txtPath = Paths.get(epiDataPath,System.currentTimeMillis() + QuestionnaireConstant.TXT).toString();
        File epiDataDirectory = new File(epiDataPath);
        if (!epiDataDirectory.exists()){
            FileUtil.mkdir(epiDataDirectory);
        }

        // 先从数据转成txt文件，再转到rec文件
        boolean isSuccess = createTxt(mergeDataTxt(headerList, dataList), txtPath);
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
        boolean isSuccess = createTxt(mergeDataTxt(headerList,dataList), txtPath);
        if (isSuccess){
            return txtPath;
        }
        return StrUtil.EMPTY;
    }

    /**
     * 把两个List数据合并成txt所需要的指定的格式
     * @param headerList rec文件的字段属性
     * @param dataList 需要导出的数据
     */
    public static List<String> mergeDataTxt(List<String> headerList, List<List<String>> dataList){
        List<String> list = new ArrayList<>();
        list.add(String.join(SEMICOLON, headerList));
        dataList.forEach(itemList -> list.add(String.join(SEMICOLON, itemList)));
        return list;
    }

    /**
     * 把内容写到指定的txt文件
     * @param dataList  需要导出的数据
     * @param filePath 写入文件地址
     */
    private static boolean createTxt(List<String> dataList, String filePath) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), GBK))) {
            for (String data : dataList) {
                bw.write(data);
                bw.write("\r\n");
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
        if (!Objects.equals(windowsSystem(), Boolean.TRUE)) {
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

    /**
     * 判断是否windows系统
     */
    private static Boolean windowsSystem(){
        String system = System.getProperty("os.name").toLowerCase();
        return !Objects.equals("mac", system) && !Objects.equals("linux", system);
    }

    /**
     * S3链接下载rec文件
     *
     * @param recUrl S3的rec文件链接
     */
    public static String getRecPath(String recUrl, String epiDataPath, String fileName) {
        String recPath = Paths.get(epiDataPath, fileName + QuestionnaireConstant.ZIP).toString();
        try {
            FileUtils.copyURLToFile(new URL(recUrl), new File(recPath));
        } catch (IOException e) {
            throw new BusinessException("create rec file failed, recUrl=" + recUrl);
        }
        return recPath;
    }

    public static void main(String[] args) {
        qw();
        cc();
    }

    private static void qw() {
        String txt ="/Users/yuanhang/Desktop/ZYHT/coding/myopia-pac-backend/myopia-business/business-core/questionnaire-core/src/main/java/com/wupol/myopia/business/core/questionnaire/hz-学校2的学生健康状况及影响因素调查表（小学版）的rec文件.txt";

//        List<String> qes = Lists.newArrayList();
//        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(txt),GBK))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                qes.add(line);
//            }
//        }catch (IOException e){
//            Thread.currentThread().interrupt();
//        }
//
//        List<QesFieldDataBO> list =Lists.newArrayList();
//
//        String s1 = qes.get(0);
//        String s2 = qes.get(1);
//        String[] split = s1.split(";");
//        String[] split2 = s2.split(";");
//
//
//        for (int i = 0; i < split.length; i++) {
//            list.add(new QesFieldDataBO(split[i],split2[i]));
//        }
//        System.out.println(JSON.toJSONString(list, true));
        System.out.println(getFilecharset(FileUtil.newFile(txt)));
    }

    public void cc (){
        String qq ="[\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"id1\\\"\",\n" +
                "    \"recAnswer\":\"4201202102040012\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"province\\\"\",\n" +
                "    \"recAnswer\":\"42\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"city\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"district\\\"\",\n" +
                "    \"recAnswer\":\"2\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"county\\\"\",\n" +
                "    \"recAnswer\":\"2\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"point\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"school\\\"\",\n" +
                "    \"recAnswer\":\"2\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"date\\\"\",\n" +
                "    \"recAnswer\":\"2022/08/23\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"a01\\\"\",\n" +
                "    \"recAnswer\":\"4\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"a011\\\"\",\n" +
                "    \"recAnswer\":\"12\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"id2\\\"\",\n" +
                "    \"recAnswer\":\"4201202102040012\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"a02\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"a04\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"a05\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"a05other\\\"\",\n" +
                "    \"recAnswer\":\"\\\"\\\"\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"a06\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"a061\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"a062\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"a063\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"a064\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"a065\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"a066\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"a067\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"a08\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"a081\\\"\",\n" +
                "    \"recAnswer\":\"1.0\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"a09\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"a091\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"b01\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"b03\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"b04\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"b041\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"b05\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"b051\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"b06\\\"\",\n" +
                "    \"recAnswer\":\"2\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"b0611\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"b0612\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"b0613\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"b0614\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"b0615\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"b0616\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"b09a\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"b09a1\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"b12\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"e01\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"e011\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"e031\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"e032\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"e033\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"e034\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"e035\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"e036\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"e04\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"e05\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"e06\\\"\",\n" +
                "    \"recAnswer\":\"2\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"e061\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"e13\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"e0131\\\"\",\n" +
                "    \"recAnswer\":\"\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"f01\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"f011\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"f03\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"f031\\\"\",\n" +
                "    \"recAnswer\":\"\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"f051a\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"f052a\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"f053a\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"f054a\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"f07\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"f071\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"k01\\\"\",\n" +
                "    \"recAnswer\":\"2\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"k011\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"k02\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"k03\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"k04\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"k05\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"a07\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"a071\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"l02\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"l02a\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"l02b\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"l02c\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"l02d\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"l02e\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"l02f\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"l02g\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"l021\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"l03\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"l04\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"l05\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"l06\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"l07\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"l08\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"l09\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h401b\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h401c\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h401d\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h01b\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h02a\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h402b\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h402c\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h402d1\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h402d2\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h402d3\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h402d4\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h402d5\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"c09\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"c10\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"c102\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h403a\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h403b\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h403c\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h403d\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h403e\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h04a\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h04b\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h04c\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h04c1\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h04c2\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h03a\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h03b\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h03c\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h03d\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"c03\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"c03other\\\"\",\n" +
                "    \"recAnswer\":\"\\\"\\\"\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h03f\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h03g\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h03h\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h05a\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"d031\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"d032\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"d041\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"d042\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h06a\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h06b\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h06c\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h06d1\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h06d2\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h06d3\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h06d4\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h06d5\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h06dother\\\"\",\n" +
                "    \"recAnswer\":\"\\\"\\\"\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h06d6\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h06e1\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h06e2\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h06e3\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h06e4\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h06e7\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h06e5\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h06eother\\\"\",\n" +
                "    \"recAnswer\":\"\\\"\\\"\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h06e6\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h06f\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h06f1\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h06f2\\\"\",\n" +
                "    \"recAnswer\":\"\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h06g\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"h06g1\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"m01\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"m02\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"m03\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"m031\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"m04\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"m05\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"m06\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"m07\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"m08a\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"m08b\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"m08c\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"qesField\":\"\\\"m08d\\\"\",\n" +
                "    \"recAnswer\":\"1\"\n" +
                "  }\n" +
                "]\n";
        List<QesFieldDataBO> qesFieldDataBOList = JSON.parseArray(qq, QesFieldDataBO.class);
        List<String> qes = qesFieldDataBOList.stream().map(QesFieldDataBO::getQesField).collect(Collectors.toList());
        List<String> data = qesFieldDataBOList.stream().map(QesFieldDataBO::getRecAnswer).collect(Collectors.toList());
        List<List<String>> list = Lists.newArrayList();
        list.add(data);
        String txtPath = EpiDataUtil.createTxtPath(qes, list);
        System.out.println(txtPath);
        System.out.println(getFilecharset(FileUtil.newFile(txtPath)));

    }


    private static  String getFilecharset(File sourceFile) {
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1) {
                //文件编码为 ANSI
                return charset;
            } else if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                //文件编码为 Unicode
                charset = "UTF-16LE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF) {
                //文件编码为 Unicode big endian
                charset = "UTF-16BE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF  && first3Bytes[1] == (byte) 0xBB && first3Bytes[2] == (byte) 0xBF) {
                //文件编码为 UTF-8
                charset = "UTF-8";
                checked = true;
            }
            bis.reset();
            if (!checked) {
                int loc = 0;
                while ((read = bis.read()) != -1) {
                    loc++;
                    if (read >= 0xF0) {
                        break;
                    }
                    if (0x80 <= read && read <= 0xBF){
                        // 单独出现BF如下的，也算是GBK
                        break;
                    }
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF){
                            // 双字节 (0xC0 - 0xDF)
                            // (0x80
                            // - 0xBF),也可能在GB编码内
                            continue;
                        } else {
                            break;
                        }
                    } else if (0xE0 <= read && read <= 0xEF) {
                        // 也有可能出错，可是概率较小
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return charset;
    }
}
