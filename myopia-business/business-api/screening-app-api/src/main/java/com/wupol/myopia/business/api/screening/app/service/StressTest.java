package com.wupol.myopia.business.api.screening.app.service;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.business.api.screening.app.domain.dto.AppStudentDTO;
import com.wupol.myopia.business.api.screening.app.domain.dto.ComputerOptometryDTO;
import com.wupol.myopia.business.api.screening.app.domain.dto.VisionDataDTO;
import com.wupol.myopia.business.common.utils.constant.GlassesType;
import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021/7/21
 **/
public class StressTest {

    private static final double[] VISION = { 4.0, 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 4.9, 5.0, 5.1, 5.2, 5.3 };
    private static final double[] OPTOMETRY = { -5.00, -4.50, -4.25, -4.00, -3.75, -3.50, -3.25, -3.00, -2.75, -2.50, -2.25, -2.00,
            -1.75, -1.50, -1.25, -1.00, -0.75, -0.50, -0.25, 0.00, 0.25, 0.50, 0.75, 1.00, 1.25, 1.50, 1.75, 2.00, 2.25, 2.50  };
    private static final String[] CLASS_NAME = { "一班", "二班", "三班" };
    private static AtomicInteger planStudentId = new AtomicInteger(20058);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 生成筛查计划
        // 批量导入筛查学生
        // 批量上传筛查数据
//        String token = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySW5mbyI6eyJpZCI6NjMsIm9yZ0lkIjoxMSwidXNlcm5hbWUiOiIxODkzMzg0NTc4NCIsInN5c3RlbUNvZGUiOjMsInJvbGVUeXBlcyI6W10sInBsYXRmb3JtQWRtaW5Vc2VyIjpmYWxzZX0sImV4cCI6MTYyNjk3MDQyOSwidXNlcl9uYW1lIjoiMTg5MzM4NDU3ODQiLCJqdGkiOiJjN2ViZjliNy0yODc4LTRjZDEtYmY5MS0xYTA2YjAwMGUxZmQiLCJjbGllbnRfaWQiOiIzIiwic2NvcGUiOlsiYWxsIl19.oBoORP6CsrTUzPM6EbePseufM-jOSNFU01JcQwvfoocrOA958SmAVKL1aBbmpzS04_DVOOCRlyGhLnulJ9waQoO7-y9CNN07GFpsT7uGsUsbAC5Saauh16YZPs37i7a6bsAqAQSa5iL20KazMgzljhELRlgIYD655LzxgTyIxiNgfMwSLPVyfg3cyVmXtrSr_Jm9STo5-aLk8ISDaXrzBOIVnUNgpdFsyn0zYn6dxE6_-tZBIhhaE4LGPjBBRJLsBgtcdcWGeV0A3OxFWB03rUFrjCwl4m9Rt8mze5BZeVV4P8vubmDZNcnDjSzin9ATDchlkSZnjxxsEQtyQOHzxw";
//        String host = "https://t-myopia-pac-mgmt.tulab.cn";
        //
        // 生产
        String token = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySW5mbyI6eyJpZCI6NTgsIm9yZ0lkIjo2LCJ1c2VybmFtZSI6IjE1NDg4ODg4OTk5Iiwic3lzdGVtQ29kZSI6Mywicm9sZVR5cGVzIjpbXSwicGxhdGZvcm1BZG1pblVzZXIiOmZhbHNlfSwiZXhwIjoxNjI3MDE4OTU4LCJ1c2VyX25hbWUiOiIxNTQ4ODg4ODk5OSIsImp0aSI6IjNkZGM2ZGMzLTJhNDAtNDNhMS1hZDg2LWZhYTI5ODJmZmZjOSIsImNsaWVudF9pZCI6IjMiLCJzY29wZSI6WyJhbGwiXX0.VEmNtTXQaiQTiPp7ebOxswjGbdDU4JzuHwFmMAB1OiDRFy7fyFVhT35TFE6leIzzB_jeDXgJwlLJyp5vwGZd-zJk9u-iWYzyWmlthQm97oLcAnyL5O_aBSqJJ15-Q5x4ra-jsGnA6DuPqZxqaGmUQXxoqztC93nWy_Z7XKRXXVv8ujXm_hycHQCuPCvG4V_Ezem9MeSFz-mM8oswvfqPQaQIi6nHtNwAgLYvhYkrhyP3Dk4Kzm_B_RB2SydsHLXEc0jp99ebBh8fvosd1XPGCNw8M8hwiQCtasm66q4e3L-sh_ovRxxd9I9vfZ78Dgqk75OwEzt3FLntI07h4VX30Q";
        String host = "https://myopia-pac-mgmt.vistel.cn";

        int screeningOrgId = 6;
        int createUserId = 58;
        int schoolId = 8;
        String schoolName = "罗岗镇小学";
//        int schoolId = 7;
//        String schoolName = "罗岗镇中学";
//        int schoolId = 12;
//        String schoolName = "罗岗镇高中";
//        int schoolId = 9;
//        String schoolName = "兴县高级中学";

//        int screeningOrgId = 11;
//        int createUserId = 63;
//        int schoolId = 11;
//        String schoolName = "压测学校-武汉";
//        int schoolId = 12;
//        String schoolName = "新华中学-武汉";

        // 新增学生
//        executeAddStudent(token, host, schoolId, schoolName);
        // 视力筛查
//        executeAddVision(token, host, schoolId, screeningOrgId, createUserId);
        // 电脑验光
//        executeAddComputer(token, host, schoolId, screeningOrgId, createUserId);
        addStudent(token, host, schoolId, schoolName);
    }

    private static void executeAddStudent(String token, String host, long schoolId, String schoolName) throws ExecutionException, InterruptedException {
        execute(60, 7205, () -> {
            long begin = System.currentTimeMillis();
            addStudent(token, host, schoolId, schoolName);
            long end = System.currentTimeMillis();
            return end - begin;
        });
    }

    private static void executeAddVision(String token, String host, int schoolId, int screeningOrgId, int createUserId) throws ExecutionException, InterruptedException {
        execute(60, 12206, () -> {
            long begin = System.currentTimeMillis();
            addVision(token, host, schoolId, screeningOrgId, createUserId);
            long end = System.currentTimeMillis();
            return end - begin;
        });
    }

    private static void executeAddComputer(String token, String host, int schoolId, int screeningOrgId, int createUserId) throws ExecutionException, InterruptedException {
        execute(70, 12206, () -> {
            long begin = System.currentTimeMillis();
            addComputer(token, host, schoolId, screeningOrgId, createUserId);
            long end = System.currentTimeMillis();
            return end - begin;
        });
    }

    private static void addVision(String token, String host, int schoolId, int screeningOrgId, int createUserId){
        try {
            System.out.print(".");
            int glassesType = RandomUtil.randomInt(0, 3);
            double leftNakedVision = randomFrom(VISION);
            double rightNakedVision = randomFrom(VISION);
            VisionDataDTO visionDataDTO = new VisionDataDTO();
            visionDataDTO.setStudentId(String.valueOf(planStudentId.getAndAdd(1))).setSchoolId(String.valueOf(schoolId)).setDeptId(screeningOrgId).setCreateUserId(createUserId);
            visionDataDTO.setGlassesType(WearingGlassesSituation.getType(glassesType))
                    .setLeftNakedVision(BigDecimal.valueOf(leftNakedVision))
                    .setRightNakedVision(BigDecimal.valueOf(rightNakedVision));
            // 如果戴镜，则填充矫正视力
            if (glassesType != GlassesType.NOT_WEARING.code) {
                int leftIndex = ArrayUtil.indexOf(VISION, leftNakedVision);
                int rightIndex = ArrayUtil.indexOf(VISION, rightNakedVision);
                visionDataDTO.setLeftCorrectedVision(BigDecimal.valueOf(randomFrom(VISION, leftIndex)))
                        .setRightCorrectedVision(BigDecimal.valueOf(randomFrom(VISION, rightIndex)));
            }
            // 发送请求
            ObjectMapper objectMapper = new ObjectMapper();
            String requestData = objectMapper.writeValueAsString(visionDataDTO);
            String response = HttpRequest.post(host + "/app/screening/eye/addVision")
                    .header("Authorization", token)
                    .body(requestData).execute().body();
            ApiResult apiResult = objectMapper.readValue(response, ApiResult.class);
            if (!apiResult.isSuccess()) {
                System.out.println("请求数据：" + requestData + "，响应数据：" + response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addComputer(String token, String host, int schoolId, int screeningOrgId, int createUserId){
        try {
            System.out.print(".");
            ComputerOptometryDTO computerOptometryDTO = new ComputerOptometryDTO();
            computerOptometryDTO.setStudentId(String.valueOf(planStudentId.getAndAdd(1))).setSchoolId(String.valueOf(schoolId)).setDeptId(screeningOrgId).setCreateUserId(createUserId);
            computerOptometryDTO.setLAxial(BigDecimal.valueOf(randomFrom(OPTOMETRY)))
                    .setLCyl(BigDecimal.valueOf(randomFrom(OPTOMETRY)))
                    .setLSph(BigDecimal.valueOf(randomFrom(OPTOMETRY)))
                    .setRAxial(BigDecimal.valueOf(randomFrom(OPTOMETRY)))
                    .setRCyl(BigDecimal.valueOf(randomFrom(OPTOMETRY)))
                    .setRSph(BigDecimal.valueOf(randomFrom(OPTOMETRY)));
            ObjectMapper objectMapper = new ObjectMapper();
            String requestData = objectMapper.writeValueAsString(computerOptometryDTO);
            String response = HttpRequest.post(host + "/app/screening/eye/addComputer")
                    .header("Authorization", token)
                    .body(requestData).execute().body();
            ApiResult apiResult = objectMapper.readValue(response, ApiResult.class);
            if (!apiResult.isSuccess()) {
                System.out.println("请求数据：" + requestData + "，响应数据：" + response);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private static void addStudent(String token, String host, long schoolId, String schoolName) {
        try {
            System.out.print(".");
//            String idCard = IdCardUtil.generate();
            String idCard = "11010120130307783X";
            AppStudentDTO appStudentDTO = new AppStudentDTO()
                    .setStudentName(ChineseNameUtil.getChineseName())
                    .setSex(RandomUtil.randomString("男女", 1))
                    .setSchoolId(schoolId)
                    .setSchoolName(schoolName)
                    .setGrade(randomGradeName())
                    .setClazz(randomClassName())
                    .setIdCard(idCard)
                    .setBirthday(getBirthdayByIdCard(idCard))
                    .setStudentNo(idCard.substring(8));
            ObjectMapper objectMapper = new ObjectMapper();
            String requestData = objectMapper.writeValueAsString(appStudentDTO);
            String response = HttpRequest.post(host + "/app/screening/student/save")
                    .header("Authorization", token)
                    .body(requestData).execute().body();
            ApiResult apiResult = objectMapper.readValue(response, ApiResult.class);
            if (!apiResult.isSuccess()) {
                System.out.println("请求数据：" + requestData + "，响应数据：" + response);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static String getBirthdayByIdCard(String idCard) {
        String birthday = IdcardUtil.getBirthByIdCard(idCard);
        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder.append(birthday, 0, 4)
                .append("-")
                .append(birthday, 4, 6)
                .append("-")
                .append(birthday, 6, 8)
                .toString();
    }

    private static String randomGradeName() {
        // 小学
        List<GradeCodeEnum> gradeCodeEnumList = GradeCodeEnum.privateSchool();
        // 初中
//        List<GradeCodeEnum> gradeCodeEnumList = GradeCodeEnum.juniorSchool();
        // 高中
//        List<GradeCodeEnum> gradeCodeEnumList = GradeCodeEnum.highSchool();
        String[] gradeNameArray = gradeCodeEnumList.stream().map(GradeCodeEnum::getName).toArray(String[]::new);
        return gradeNameArray[RandomUtil.randomInt(gradeNameArray.length)];
    }

    private static String randomClassName() {
        return CLASS_NAME[RandomUtil.randomInt(CLASS_NAME.length)];
    }

    private static double randomFrom(double[] items) {
        return randomFrom(items, 0);
    }

    private static double randomFrom(double[] items, int min) {
        return items[RandomUtil.randomInt(min, items.length)];
    }

    private static void execute(int numOfThreads, int times, final Callable<Long> task) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(numOfThreads);
        List<Future<Long>> results = new ArrayList<>();
        long startTimeMillis = System.currentTimeMillis();

        System.out.println("开始并发请求：" + startTimeMillis);
        for (int i = 0; i < times; i++) {
            results.add(executor.submit(task));
        }
        System.out.println("所有请求发送完成：" + System.currentTimeMillis());

        executor.shutdown();
        while(!executor.awaitTermination(1, TimeUnit.SECONDS)) { }

        long endTimeMillis = System.currentTimeMillis();
        System.out.println("所有请求响应完成：" + endTimeMillis);

        long totalCostTimeMillis = System.currentTimeMillis() - startTimeMillis;

        List<Long> requestTimeList = results.stream().map(x -> {
            try {
                return x.get();
            } catch (Exception e) {
                return 0L;
            }
        }).collect(Collectors.toList());

        System.out.println("---------------------------------");
        System.out.println("number of threads: " + numOfThreads + ", times: " + times);
        System.out.println("running time: " + (totalCostTimeMillis / 1000) + " s, avg time: " + ((double) totalCostTimeMillis / times) + " ms");
        System.out.println("total cost time: " + requestTimeList.stream().mapToLong(Long::intValue).sum() / 1000 + " s");
        System.out.println("Min response time：" + requestTimeList.stream().mapToInt(Long::intValue).min().orElse(0) + " ms，Max response time：" + requestTimeList.stream().mapToInt(Long::intValue).max().orElse(0) + " ms");
        System.out.println("TPS: " + (double) (times * 1000) / totalCostTimeMillis);
    }

}
