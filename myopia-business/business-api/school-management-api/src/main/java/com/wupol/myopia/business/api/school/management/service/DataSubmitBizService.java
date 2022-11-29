package com.wupol.myopia.business.api.school.management.service;

import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.util.ListUtil;
import com.wupol.myopia.business.core.common.util.S3Utils;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.constant.NationalDataDownloadStatusEnum;
import com.wupol.myopia.business.core.screening.flow.domain.dto.DataSubmitExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.NationalDataDownloadRecord;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.NationalDataDownloadRecordService;
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
import java.math.RoundingMode;
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
    private NationalDataDownloadRecordService nationalDataDownloadRecordService;

    @Resource
    private S3Utils s3Utils;

    @Resource
    private NoticeService noticeService;

    @Resource
    private VisionScreeningResultService visionScreeningResultService;

    @Resource
    private StudentService studentService;

    /**
     * 处理数据上报
     */
    @Async
    public void dataSubmit(List<Map<Integer, String>> listMap, Integer dataSubmitId, Integer userId, Integer schoolId) {
        NationalDataDownloadRecord nationalDataDownloadRecord = nationalDataDownloadRecordService.getById(dataSubmitId);
        try {
            dealDataSubmit(listMap, nationalDataDownloadRecord, userId, schoolId);
        } catch (Exception e) {
            log.error("处理数据上报异常", e);
            noticeService.createExportNotice(userId, userId, CommonConst.ERROR, CommonConst.ERROR, null, CommonConst.NOTICE_STATION_LETTER);
            nationalDataDownloadRecord.setStatus(NationalDataDownloadStatusEnum.FAIL.getType());
            nationalDataDownloadRecordService.updateById(nationalDataDownloadRecord);
        }
    }

    /**
     * 生成Excel文件
     */
    private void dealDataSubmit(List<Map<Integer, String>> listMap, NationalDataDownloadRecord nationalDataDownloadRecord, Integer userId, Integer schoolId) throws IOException, UtilException {

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);

        Map<String, VisionScreeningResult> screeningData = getScreeningData(listMap, schoolId);

        List<DataSubmitExportDTO> collect = listMap.stream().map(s -> {
            DataSubmitExportDTO exportDTO = new DataSubmitExportDTO();
            getOriginalInfo(s, exportDTO);
            getScreeningInfo(success, fail, screeningData, s, exportDTO);
            return exportDTO;
        }).collect(Collectors.toList());
        File excel = ExcelUtil.exportListToExcel(CommonConst.FILE_NAME, collect, DataSubmitExportDTO.class);
        Integer fileId = s3Utils.uploadFileToS3(excel);
        nationalDataDownloadRecord.setSuccessMatch(success.get());
        nationalDataDownloadRecord.setFailMatch(fail.get());
        nationalDataDownloadRecord.setFileId(fileId);
        nationalDataDownloadRecord.setStatus(NationalDataDownloadStatusEnum.SUCCESS.getType());
        nationalDataDownloadRecordService.updateById(nationalDataDownloadRecord);
        noticeService.createExportNotice(userId, userId, CommonConst.SUCCESS, CommonConst.SUCCESS, fileId, CommonConst.NOTICE_STATION_LETTER);
    }

    /**
     * 通过学号获取筛查信息
     */
    private Map<String, VisionScreeningResult> getScreeningData(List<Map<Integer, String>> listMap, Integer schoolId) {
        List<String> snoList = listMap.stream().map(s -> s.get(3)).collect(Collectors.toList());
        List<Student> studentList = studentService.getLastBySno(snoList, schoolId);
        Map<Integer, VisionScreeningResult> resultMap = visionScreeningResultService.getLastByStudentIds(studentList.stream().map(Student::getId).collect(Collectors.toList()), schoolId);
        return studentList.stream().filter(ListUtil.distinctByKey(Student::getSno))
                .filter(s -> StringUtils.isNotBlank(s.getSno()))
                .collect(Collectors.toMap(Student::getSno, s -> resultMap.getOrDefault(s.getId(), new VisionScreeningResult())));
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

    /**
     * 获取筛查信息
     */
    private void getScreeningInfo(AtomicInteger success, AtomicInteger fail, Map<String, VisionScreeningResult> screeningResultMap, Map<Integer, String> s, DataSubmitExportDTO exportDTO) {
        VisionScreeningResult result = screeningResultMap.get(s.get(3));
        if (Objects.nonNull(result) && Objects.nonNull(result.getId())) {
            exportDTO.setRightNakedVision(getNakedVision(EyeDataUtil.rightNakedVision(result)));
            exportDTO.setLeftNakedVision(getNakedVision(EyeDataUtil.leftNakedVision(result)));
            exportDTO.setRightSph(EyeDataUtil.spliceSymbol(EyeDataUtil.rightSph(result)));
            exportDTO.setRightCyl(EyeDataUtil.spliceSymbol(EyeDataUtil.rightCyl(result)));
            exportDTO.setRightAxial(EyeDataUtil.rightAxial(result).toString());
            exportDTO.setLeftSph(EyeDataUtil.spliceSymbol(EyeDataUtil.leftSph(result)));
            exportDTO.setLeftCyl(EyeDataUtil.spliceSymbol(EyeDataUtil.leftCyl(result)));
            exportDTO.setLeftAxial(EyeDataUtil.leftAxial(result).toString());
            exportDTO.setIsOk(Objects.equals(EyeDataUtil.glassesType(result), GlassesTypeEnum.ORTHOKERATOLOGY.code) ? "是" : "否");
            success.incrementAndGet();
        } else {
            fail.incrementAndGet();
        }
    }

    /**
     * 处理裸眼视力
     *
     * @param nakedVision 裸眼视力
     *
     * @return 裸眼视力
     */
    private String getNakedVision(BigDecimal nakedVision) {
        if (Objects.isNull(nakedVision)) {
            return StringUtils.EMPTY;
        }
        if (BigDecimalUtil.lessThan(nakedVision, "3.0")) {
            return "9.0";
        }
        return nakedVision.setScale(1, RoundingMode.DOWN).toString();
    }
}