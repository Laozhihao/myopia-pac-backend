package com.wupol.myopia.business.api.school.management.service;

import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.util.FileUtils;
import com.wupol.myopia.business.core.common.util.S3Utils;
import com.wupol.myopia.business.core.screening.flow.domain.dto.DataSubmitExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.DataSubmit;
import com.wupol.myopia.business.core.screening.flow.service.DataSubmitService;
import com.wupol.myopia.business.core.system.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
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

    @Async
    public void dataSubmit(MultipartFile multipartFile, Integer dataSubmitId, Integer userId) {
        DataSubmit dataSubmit = dataSubmitService.getById(dataSubmitId);
        try {
            dealDataSubmit(multipartFile, dataSubmit, userId);
        } catch (Exception e) {
            log.error("处理数据上报异常", e);
            noticeService.createExportNotice(userId, userId, CommonConst.ERROR, CommonConst.ERROR, null, CommonConst.NOTICE_STATION_LETTER);
            dataSubmit.setDownloadMessage("系统错误，请重试");
        }
    }

    private void dealDataSubmit(MultipartFile multipartFile, DataSubmit dataSubmit, Integer userId) throws IOException, UtilException {
        List<Map<Integer, String>> listMap = FileUtils.readExcel(multipartFile);

        List<DataSubmitExportDTO> collect = listMap.stream().map(s -> {
            DataSubmitExportDTO exportDTO = new DataSubmitExportDTO();

            exportDTO.setGradeCode(s.get(0));
            exportDTO.setClassCode(s.get(1));
            exportDTO.setClassName(s.get(2));
            exportDTO.setStudentNo(s.get(3));
            exportDTO.setNation(s.get(4));
            exportDTO.setName(s.get(5));
            exportDTO.setGender(s.get(6));
            exportDTO.setBirthday(s.get(7));
            exportDTO.setAddress(s.get(8));

            exportDTO.setRighta("1");
            exportDTO.setRightb("1");
            exportDTO.setRightc("1");
            exportDTO.setLefta("1");
            exportDTO.setLeftb("1");
            exportDTO.setLeftc("1");
            exportDTO.setIsOk("1");

            return exportDTO;
        }).collect(Collectors.toList());
        File excel = ExcelUtil.exportListToExcel(CommonConst.FILE_NAME, collect, DataSubmitExportDTO.class);
        Integer fileId = s3Utils.uploadFileToS3(excel);
        dataSubmit.setSuccessMatch(collect.size());
        dataSubmit.setFailMatch(collect.size());
        dataSubmit.setFileId(fileId);
        dataSubmitService.updateById(dataSubmit);
        noticeService.createExportNotice(userId, userId, CommonConst.SUCCESS, CommonConst.SUCCESS, fileId, CommonConst.NOTICE_STATION_LETTER);
    }
}