package com.wupol.myopia.business.api.management.service.stressTest;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.core.school.domain.dto.SchoolClassExportDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeExportDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ComputerOptometryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021/7/21
 **/
@Log4j2
@Service
public class StressTestService {

    private static final double[] OPTOMETRY = { -5.00, -4.50, -4.25, -4.00, -3.75, -3.50, -3.25, -3.00, -2.75, -2.50, -2.25, -2.00,
            -1.75, -1.50, -1.25, -1.00, -0.75, -0.50, -0.25, 0.00, 0.25, 0.50, 0.75, 1.00, 1.25, 1.50, 1.75, 2.00, 2.25, 2.50  };
    private static final AtomicInteger planStudentId = new AtomicInteger(20058);
    /** 批次号 */
    private static final String BATCH_NO = "02";


    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private ScreeningPlanSchoolService screeningPlanSchoolService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private InsertDataService insertDataService;

    /**
     * 批量生成数据
     *
     * @throws SQLException
     * @throws InterruptedException
     */
    public void generateDataBatch() throws SQLException, InterruptedException {
        log.info("开始......");
        long start = System.currentTimeMillis();
        // 1. 获取筛查机构
        List<ScreeningOrganization> organizationList = screeningOrganizationService.getByName("-" + BATCH_NO).stream().filter(x -> x.getName().contains("压测筛查机构-")).collect(Collectors.toList());
        List<Integer> orgIdList = organizationList.stream().map(ScreeningOrganization::getId).collect(Collectors.toList());
        // 2. 获取筛查计划
        List<ScreeningPlan> planList = screeningPlanService.getReleasePlanByOrgIds(orgIdList, ScreeningTypeEnum.VISION.getType());
        Map<Integer, ScreeningPlan> planMap = planList.stream().collect(Collectors.toMap(ScreeningPlan::getId, Function.identity()));
        // 3. 获取计划下的所有筛查学校
        List<ScreeningPlanSchool> planSchoolList = screeningPlanSchoolService.getByPlanIds(new ArrayList<>(planMap.keySet()));
        List<Integer> schoolIdList = planSchoolList.stream().map(ScreeningPlanSchool::getSchoolId).distinct().collect(Collectors.toList());
        Map<Integer, School> schoolMap = schoolService.getByIds(schoolIdList).stream().collect(Collectors.toMap(School::getId, Function.identity()));
        // 4. 获取每个计划的年级、班级信息
        List<SchoolGradeExportDTO> gradeList = schoolGradeService.getBySchoolIds(schoolIdList);
        Map<Integer, List<SchoolGradeExportDTO>> gradeMap = gradeList.stream().collect(Collectors.groupingBy(SchoolGradeExportDTO::getSchoolId));
        List<SchoolClassExportDTO> classList = schoolClassService.getByGradeIds(gradeList.stream().map(SchoolGradeExportDTO::getId).collect(Collectors.toList()));
        Map<Integer, List<SchoolClassExportDTO>> classMap = classList.stream().collect(Collectors.groupingBy(SchoolClassExportDTO::getGradeId));
        // 5. 批量插入数据
        int schoolNumPerThread = 3000;
        int threadTotal = planSchoolList.size() / schoolNumPerThread;
        CountDownLatch latch = new CountDownLatch(threadTotal);
        for (int i = 0; i < threadTotal; i++) {
            log.info("第{}批，开始......", i);
            insertDataService.insertDataBatch(planSchoolList, i, latch, schoolNumPerThread, planMap, schoolMap, gradeMap, classMap);
        }
        latch.await();
        long end = System.currentTimeMillis() - start;
        log.info("耗时：" + end / (1000 * 60) + "分钟");
    }


    /**
     * 通过http请求批量生成数据
     *
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        String token = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySW5mbyI6eyJpZCI6NTgsIm9yZ0lkIjo2LCJ1c2VybmFtZSI6IjE1NDg4ODg4OTk5Iiwic3lzdGVtQ29kZSI6Mywicm9sZVR5cGVzIjpbXSwicGxhdGZvcm1BZG1pblVzZXIiOmZhbHNlfSwiZXhwIjoxNjI3MDE4OTU4LCJ1c2VyX25hbWUiOiIxNTQ4ODg4ODk5OSIsImp0aSI6IjNkZGM2ZGMzLTJhNDAtNDNhMS1hZDg2LWZhYTI5ODJmZmZjOSIsImNsaWVudF9pZCI6IjMiLCJzY29wZSI6WyJhbGwiXX0.VEmNtTXQaiQTiPp7ebOxswjGbdDU4JzuHwFmMAB1OiDRFy7fyFVhT35TFE6leIzzB_jeDXgJwlLJyp5vwGZd-zJk9u-iWYzyWmlthQm97oLcAnyL5O_aBSqJJ15-Q5x4ra-jsGnA6DuPqZxqaGmUQXxoqztC93nWy_Z7XKRXXVv8ujXm_hycHQCuPCvG4V_Ezem9MeSFz-mM8oswvfqPQaQIi6nHtNwAgLYvhYkrhyP3Dk4Kzm_B_RB2SydsHLXEc0jp99ebBh8fvosd1XPGCNw8M8hwiQCtasm66q4e3L-sh_ovRxxd9I9vfZ78Dgqk75OwEzt3FLntI07h4VX30Q";
        String host = "https://myopia-pac-mgmt.vistel.cn";
        int screeningOrgId = 6;
        int createUserId = 58;
        int schoolId = 8;
        // 电脑验光
        executeAddComputer(token, host, schoolId, screeningOrgId, createUserId);
    }

    private static void executeAddComputer(String token, String host, int schoolId, int screeningOrgId, int createUserId) throws InterruptedException {
        execute(70, 12206, () -> {
            long begin = System.currentTimeMillis();
            addComputer(token, host, schoolId, screeningOrgId, createUserId);
            long end = System.currentTimeMillis();
            return end - begin;
        });
    }

    private static void addComputer(String token, String host, int schoolId, int screeningOrgId, int createUserId){
        try {
            System.out.print(".");
            ComputerOptometryDTO computerOptometryDTO = new ComputerOptometryDTO();
            computerOptometryDTO.setPlanStudentId(String.valueOf(planStudentId.getAndAdd(1))).setSchoolId(String.valueOf(schoolId)).setDeptId(screeningOrgId).setCreateUserId(createUserId);
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

    private static double randomFrom(double[] items) {
        return randomFrom(items, 0);
    }

    private static double randomFrom(double[] items, int min) {
        return items[RandomUtil.randomInt(min, items.length)];
    }

    private static void execute(int numOfThreads, int times, final Callable<Long> task) throws InterruptedException {
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
