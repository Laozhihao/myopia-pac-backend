package com.wupol.myopia.business.api.school.management.service;

import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.util.ListUtil;
import com.wupol.myopia.business.core.common.util.S3Utils;
import com.wupol.myopia.business.core.screening.flow.domain.dto.DataSubmitExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.DataSubmit;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.DataSubmitService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.EyeDataUtil;
import com.wupol.myopia.business.core.system.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 数据上传
 *
 * @author Simple4H
 */
@Service
@Slf4j
public class DataSubmitBizService {

    @Resource
    private DataSubmitService dataSubmitService;

    @Resource
    private S3Utils s3Utils;

    @Resource
    private NoticeService noticeService;

    @Resource
    private VisionScreeningResultService visionScreeningResultService;

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Async
    public void dataSubmit(List<Map<Integer, String>> listMap, Integer dataSubmitId, Integer userId) {
        DataSubmit dataSubmit = dataSubmitService.getById(dataSubmitId);
        try {
            dealDataSubmit(listMap, dataSubmit, userId);
        } catch (Exception e) {
            log.error("处理数据上报异常", e);
            noticeService.createExportNotice(userId, userId, CommonConst.ERROR, CommonConst.ERROR, null, CommonConst.NOTICE_STATION_LETTER);
            dataSubmit.setDownloadMessage("系统错误，请重试");
            dataSubmitService.updateById(dataSubmit);
        }
    }

    private void dealDataSubmit(List<Map<Integer, String>> listMap, DataSubmit dataSubmit, Integer userId) throws IOException, UtilException {

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);

        Map<String, VisionScreeningResult> screeningData = getScreeningData(listMap);

        List<DataSubmitExportDTO> collect = listMap.stream().map(s -> {
            DataSubmitExportDTO exportDTO = new DataSubmitExportDTO();
            getOriginalInfo(s, exportDTO);
            getScreeningInfo(success, fail, screeningData, s, exportDTO);
            return exportDTO;
        }).collect(Collectors.toList());
        File excel = ExcelUtil.exportListToExcel(CommonConst.FILE_NAME, collect, DataSubmitExportDTO.class);
        Integer fileId = s3Utils.uploadFileToS3(excel);
        dataSubmit.setSuccessMatch(success.get());
        dataSubmit.setFailMatch(fail.get());
        dataSubmit.setFileId(fileId);
        dataSubmitService.updateById(dataSubmit);
        noticeService.createExportNotice(userId, userId, CommonConst.SUCCESS, CommonConst.SUCCESS, fileId, CommonConst.NOTICE_STATION_LETTER);
    }

    /**
     * 获取筛查信息
     */
    private void getScreeningInfo(AtomicInteger success, AtomicInteger fail, Map<String, VisionScreeningResult> screeningResultMap, Map<Integer, String> s, DataSubmitExportDTO exportDTO) {
        VisionScreeningResult result = screeningResultMap.get(s.get(3));
        if (Objects.nonNull(result.getId())) {
            exportDTO.setRightNakedVision(getNakedVision(EyeDataUtil.rightNakedVision(result)));
            exportDTO.setLeftNakedVision(getNakedVision(EyeDataUtil.leftNakedVision(result)));
            exportDTO.setRightSph(EyeDataUtil.rightSph(result).toString());
            exportDTO.setRightCyl(EyeDataUtil.rightCyl(result).toString());
            exportDTO.setRightAxial(EyeDataUtil.rightAxial(result).toString());
            exportDTO.setLeftSph(EyeDataUtil.leftSph(result).toString());
            exportDTO.setLeftCyl(EyeDataUtil.leftCyl(result).toString());
            exportDTO.setLeftAxial(EyeDataUtil.leftAxial(result).toString());
            exportDTO.setIsOk(Objects.equals(EyeDataUtil.glassesType(result), GlassesTypeEnum.ORTHOKERATOLOGY.code) ? "是" : "否");
            success.incrementAndGet();
        } else {
            fail.incrementAndGet();
        }
    }

    private String getNakedVision(BigDecimal nakedVision) {
        if (Objects.isNull(nakedVision)) {
            return StringUtils.EMPTY;
        }
        if (BigDecimalUtil.lessThan(nakedVision, "3.0")) {
            return "9";
        }
        return nakedVision.toString();
    }

    /**
     * 通过学号获取筛查信息
     */
    private Map<String, VisionScreeningResult> getScreeningData(List<Map<Integer, String>> listMap) {
        List<String> snoList = listMap.stream().map(s -> s.get(3)).collect(Collectors.toList());
        List<ScreeningPlanSchoolStudent> planStudentList = screeningPlanSchoolStudentService.getLastBySno(snoList);
        Map<Integer, VisionScreeningResult> resultMap = visionScreeningResultService.getLastByStudentIds(planStudentList.stream().map(ScreeningPlanSchoolStudent::getStudentId).collect(Collectors.toList()));
        return planStudentList.stream()
                .filter(ListUtil.distinctByKey(ScreeningPlanSchoolStudent::getStudentNo))
                .filter(s -> StringUtils.isNotBlank(s.getStudentNo()))
                .collect(Collectors.toMap(ScreeningPlanSchoolStudent::getStudentNo, s -> resultMap.getOrDefault(s.getStudentId(), new VisionScreeningResult())));
    }

    /**
     * 获取原始数据
     */
    private void getOriginalInfo(Map<Integer, String> s, DataSubmitExportDTO exportDTO) {
        exportDTO.setGradeCode(s.get(0));
        exportDTO.setClassCode(s.get(1));
        exportDTO.setClassName(s.get(2));
        exportDTO.setStudentNo(s.get(3));
        exportDTO.setNation(s.get(4));
        exportDTO.setName(s.get(5));
        exportDTO.setGender(s.get(6));
        exportDTO.setBirthday(s.get(7));
        exportDTO.setAddress(s.get(8));
    }
}