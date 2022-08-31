package com.wupol.myopia.business.aggregation.export.excel;

import com.alibaba.excel.write.merge.OnceAbsoluteMergeStrategy;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.business.aggregation.export.excel.config.ScreeningDataFactory;
import com.wupol.myopia.business.aggregation.export.service.IScreeningDataService;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.common.util.S3Utils;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningDataContrastDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.VisionScreeningResultExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.system.service.NoticeService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 统一处理 Excel 上传/下载
 *
 * @Author HaoHao
 * @Date 2020/12/21
 **/
@Log4j2
@Service
public class ExcelFacade {

    @Autowired
    private NoticeService noticeService;
    @Resource
    private S3Utils s3Utils;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ScreeningPlanService screeningPlanService;

    @Resource
    private ScreeningDataFactory screeningDataFactory;

    /**
     * 导出统计报表 - 数据对比表
     *
     * @param userId     用户ID
     * @param exportList 导出数据
     * @param template   导出模板
     * @throws IOException   IO异常
     * @throws UtilException 工具异常
     */
    public void exportStatContrast(Integer userId, List<ScreeningDataContrastDTO> exportList, InputStream template) throws IOException, UtilException {
        String fileName = "统计对比报表";
        log.info("导出统计对比报文件: {}", fileName);
        File file = ExcelUtil.exportHorizonListToExcel(fileName, exportList, template);
        String content = String.format(CommonConst.EXPORT_MESSAGE_CONTENT_SUCCESS, fileName, new Date());
        noticeService.createExportNotice(userId, userId, content, content, s3Utils.uploadFileToS3(file), CommonConst.NOTICE_STATION_LETTER);
    }

    /**
     * 导出筛查数据
     *
     * @param userId                   用户Id
     * @param statConclusionExportDTOs 导出筛查学生
     * @param isSchoolExport           是否学校维度导出
     * @param districtOrSchoolName     行政区域或学校名称
     * @param redisKey                 缓存值
     * @throws IOException   IO异常
     * @throws UtilException 工具异常
     */
    @Async
    public void generateVisionScreeningResult(Integer userId, List<StatConclusionExportDTO> statConclusionExportDTOs, boolean isSchoolExport, String districtOrSchoolName, String redisKey) throws IOException, UtilException {
        // 设置导出的文件名
        String fileName = String.format("%s筛查学生数据表", districtOrSchoolName);
        String content = String.format(CommonConst.EXPORT_MESSAGE_CONTENT_SUCCESS, districtOrSchoolName + "筛查数据", new Date());
        log.info("导出筛查结果文件: {}", fileName);
        // 通过筛查类型获取实现
        Integer planId = statConclusionExportDTOs.get(0).getPlanId();
        ScreeningPlan plan = screeningPlanService.getById(planId);
        IScreeningDataService screeningDataService = screeningDataFactory.getScreeningDataService(plan.getScreeningType());

        OnceAbsoluteMergeStrategy mergeStrategy = new OnceAbsoluteMergeStrategy(0, 1, 20, 21);
        if (isSchoolExport) {
            List<VisionScreeningResultExportDTO> visionScreeningResultExportVos = screeningDataService.generateExportData(statConclusionExportDTOs);
            visionScreeningResultExportVos.sort(Comparator.comparing((VisionScreeningResultExportDTO exportDTO) -> Integer.valueOf(GradeCodeEnum.getByName(exportDTO.getGradeName()).getCode())));
            File excelFile = ExcelUtil.exportListToExcel(fileName, visionScreeningResultExportVos, mergeStrategy, screeningDataService.getExportClass());
            noticeService.createExportNotice(userId, userId, content, content, s3Utils.uploadFileToS3(excelFile), CommonConst.NOTICE_STATION_LETTER);
        } else {
            String filePath = String.format("%s-%s", System.currentTimeMillis(), UUID.randomUUID());
            Map<String, List<StatConclusionExportDTO>> schoolNameMap = statConclusionExportDTOs.stream().collect(Collectors.groupingBy(StatConclusionExportDTO::getSchoolName));
            schoolNameMap.keySet().forEach(schoolName -> {
                StringBuilder folder = new StringBuilder();
                List<StatConclusionExportDTO> orDefault = schoolNameMap.getOrDefault(schoolName, Collections.emptyList());
                //学校的区域id，以及该区域的上层id
                if (Objects.nonNull(orDefault) && !orDefault.isEmpty()) {
                    folder.append(filePath);
                    folder.append("/").append(fileName);
                }
                List<VisionScreeningResultExportDTO> visionScreeningResultExportVos = screeningDataService.generateExportData(statConclusionExportDTOs);
                visionScreeningResultExportVos.sort(Comparator.comparing((VisionScreeningResultExportDTO exportDTO) -> Integer.valueOf(GradeCodeEnum.getByName(exportDTO.getGradeName()).getCode())));
                String excelFileName = String.format("%s筛查学生数据", schoolName);
                try {
                    ExcelUtil.exportListToExcelWithFolder(folder.toString(), excelFileName, visionScreeningResultExportVos, mergeStrategy, screeningDataService.getExportClass());
                } catch (Exception e) {
                    redisUtil.del(redisKey);
                    log.error(e);
                }
            });
            File zipFile = ExcelUtil.zip(filePath, fileName);
            noticeService.createExportNotice(userId, userId, content, content, s3Utils.uploadFileToS3(zipFile), CommonConst.NOTICE_STATION_LETTER);
        }
        redisUtil.del(redisKey);
    }
}