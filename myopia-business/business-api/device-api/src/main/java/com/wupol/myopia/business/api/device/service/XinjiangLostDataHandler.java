package com.wupol.myopia.business.api.device.service;

import cn.hutool.core.text.csv.*;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.business.aggregation.screening.domain.dto.DeviceDataRequestDTO;
import com.wupol.myopia.business.api.device.domain.dto.VisionDataVO;
import com.wupol.myopia.business.api.device.util.ParsePlanStudentUtils;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.util.Assert;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class XinjiangLostDataHandler {

    public static void main(String[] args) {
        log.info("test");
        String basePath = "D:\\App\\WXWork\\data\\WXWork\\1688850982892065\\Cache\\File\\2023-06\\11f13d9593493a4c\\11f13d9593493a4c";
        CsvReadConfig csvReadConfig = new CsvReadConfig();
        csvReadConfig.setContainsHeader(true);
        File[] files = new File(basePath).listFiles();
        Assert.notNull(files, "文件不能为空");
        List<DeviceDataRequestDTO> allDataList = new ArrayList<>();
        for (File file: files) {
            CsvReader reader = CsvUtil.getReader(csvReadConfig);
            // 从文件中读取CSV数据
            CsvData csvData = reader.read(file);
            List<CsvRow> rows = csvData.getRows();
            // 提取筛查数据
            allDataList.addAll(resolveLogToDeviceData(rows));
        }
        log.info("===================================");
        // 所有数据最后一起再次去重
        List<DeviceDataRequestDTO> totalDistinctDataList = distinctDataList(allDataList);
        // 发送请求，推送到服务器
        sendData(totalDistinctDataList);
    }

    private static void sendData(List<DeviceDataRequestDTO> totalDistinctDataList) {
        log.info("开始");
        int success = 0;
        int failed = 0;
        for (DeviceDataRequestDTO deviceData: totalDistinctDataList) {
            log.info(".");
            ApiResult apiResult = sendRequest(deviceData);
            if (Objects.isNull(apiResult) || Boolean.FALSE.equals(apiResult.isSuccess())) {
                failed++;
            } else {
                success++;
            }
        }
        log.info("结束。");
        log.info("成功：" + success);
        log.info("失败：" + failed);
    }

    private static List<DeviceDataRequestDTO> resolveLogToDeviceData(List<CsvRow> rows) {
        log.info("总行数：" + rows.size());
        List<DeviceDataRequestDTO> dataList = new ArrayList<>();
        // 遍历每一行提取数据
        for (CsvRow csvRow : rows) {
            String logData = csvRow.getRawList().get(0);
            if (!logData.startsWith("[ERROR] ") || logData.contains("获取学生信息-上传数据失败")) {
                continue;
            }
            // 截取筛查数据
            String data = subData(logData);
            // 转为实体对象
            try {
                dataList.add(parseToEntity(data));
            } catch (Exception e) {
                log.info("logData = " + logData);
                log.info("data = " + data);
                throw new RuntimeException(e);
            }
        }
        // 去重
        return distinctDataList(dataList);
    }

    private static String subData(String log) {
        String data;
        if (log.contains("数据 = ")) {
            data = log.substring(log.indexOf("数据 = ") + "数据 = ".length());
        } else if (log.contains("原始数据:")) {
            data = log.substring(log.indexOf("原始数据:") + "原始数据:".length());
        } else {
            throw new IllegalArgumentException("无效格式日志, log = " + log);
        }
        if (StringUtils.hasText(data)) {
            return data;
        }
        throw new IllegalArgumentException("无效格式日志, log = " + log);
    }

    private static DeviceDataRequestDTO parseToEntity(String data) {
        DeviceDataRequestDTO deviceDataRequestDTO = JSON.parseObject(data.replaceAll("\"\"", "\""), DeviceDataRequestDTO.class);
        Assert.notNull(deviceDataRequestDTO.getData(), "筛查数据为空");
        return deviceDataRequestDTO;
    }

    private static List<DeviceDataRequestDTO> distinctDataList(List<DeviceDataRequestDTO> dataList) {
        log.info("有效数据：" + dataList.size());
        List<DeviceDataRequestDTO> distinctDataList = dataList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(s -> getPlanStudentId(s.getData())))), ArrayList::new));
        log.info("去重后总行数：" + distinctDataList.size());
        return distinctDataList;
    }

    private static Integer getPlanStudentId(String data) {
        List<VisionDataVO> visionDataList = JSON.parseArray(data, VisionDataVO.class);
        if (visionDataList.size() > 1) {
            log.info("visionDataList集合超过1条, data = " + data);
        }
        VisionDataVO visionDataVO = visionDataList.get(0);
        return Objects.nonNull(visionDataVO.getPlanStudentId()) ? visionDataVO.getPlanStudentId() : ParsePlanStudentUtils.parsePlanStudentId(visionDataVO.getUid());
    }

    private static ApiResult sendRequest(DeviceDataRequestDTO deviceDataRequestDTO){
        String host = "https://myopia-pac.vistel.cn/mgmt/api";
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String requestData = objectMapper.writeValueAsString(deviceDataRequestDTO);
            String response = HttpRequest.post(host + "/api/device/uploadData")
                    .body(requestData).execute().body();
            return objectMapper.readValue(response, ApiResult.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
