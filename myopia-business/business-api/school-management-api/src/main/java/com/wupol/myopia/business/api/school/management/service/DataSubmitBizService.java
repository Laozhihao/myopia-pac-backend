package com.wupol.myopia.business.api.school.management.service;

import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.screening.flow.constant.NationalDataDownloadStatusEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.NationalDataDownloadRecord;
import com.wupol.myopia.business.core.screening.flow.service.NationalDataDownloadRecordService;
import com.wupol.myopia.business.core.system.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

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
    private NoticeService noticeService;

    @Resource
    private VisionScreeningService visionScreeningService;


    /**
     * 处理数据上报
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void dataSubmit(List<Map<Integer, String>> listMap, Integer dataSubmitId, Integer userId, Integer schoolId) {
        NationalDataDownloadRecord nationalDataDownloadRecord = nationalDataDownloadRecordService.getById(dataSubmitId);
        try {
            visionScreeningService.dealDataSubmit(listMap, nationalDataDownloadRecord, userId, schoolId);
        } catch (Exception e) {
            log.error("处理数据上报异常", e);
            noticeService.createExportNotice(userId, userId, CommonConst.ERROR, CommonConst.ERROR, null, CommonConst.NOTICE_STATION_LETTER);
            nationalDataDownloadRecord.setStatus(NationalDataDownloadStatusEnum.FAIL.getType());
            nationalDataDownloadRecordService.updateById(nationalDataDownloadRecord);
        }
    }
}