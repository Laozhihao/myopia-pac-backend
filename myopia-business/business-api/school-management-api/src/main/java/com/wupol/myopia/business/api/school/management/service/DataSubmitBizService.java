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

    private static final String SUCCESS = "【导出成功】数据报送数据表填写完成，点击下载或点击数据报送功能菜单查看/下载导出情况和下载数据表";

    private static final String ERROR = "【导出失败】尊敬的用户，非常抱歉通知您，因为系统问题导致视力筛查数据表填写失败，请前往数据报送功能菜单重新创建任务。如果多次出现该情况，请联系管理员，为您带来不便，我们深表歉意。";

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
            dealDataSubmit(multipartFile, dataSubmit);
            noticeService.createExportNotice(userId, userId, SUCCESS, SUCCESS, null, CommonConst.NOTICE_STATION_LETTER);
        } catch (Exception e) {
            log.error("处理数据上报异常", e);
            noticeService.createExportNotice(userId, userId, SUCCESS, SUCCESS, null, CommonConst.NOTICE_STATION_LETTER);
            dataSubmit.setDownloadMessage("系统错误，请重试");
        }
    }

    private void dealDataSubmit(MultipartFile multipartFile, DataSubmit dataSubmit) throws IOException, UtilException {
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
            exportDTO.setRight(s.get(9));
            return exportDTO;
        }).collect(Collectors.toList());
        File excel = ExcelUtil.exportListToExcel("视力数据采集表", collect, DataSubmitExportDTO.class);
        Integer fileId = s3Utils.uploadFileToS3(excel);
        dataSubmit.setSuccessMatch(collect.size());
        dataSubmit.setFailMatch(collect.size());
        dataSubmit.setFileId(fileId);
        dataSubmitService.updateById(dataSubmit);
    }
}