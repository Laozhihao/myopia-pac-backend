package com.wupol.myopia.business.aggregation.export.schedule;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.domain.vo.PdfGeneratorVO;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.pdf.domain.QueueInfo;
import com.wupol.myopia.business.core.system.service.NoticeService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 导出任务
 *
 * @author Simple4H
 */
@Component
@Log4j2
public class ExportScheduleExecutor {

    @Autowired
    private ExportStrategy exportStrategy;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private NoticeService noticeService;


    @Scheduled(cron = "0/5 * * * * ?")
    public void export() {

        // 从队列中获取一个任务
        QueueInfo queueInfo  = (QueueInfo) redisUtil.lGet(RedisConstant.FILE_EXPORT_LIST);

        if (Objects.isNull(queueInfo)) {
            return;
        }

        // 导出文件
        exportStrategy.exportFile(queueInfo);
    }

    /**
     * 异步导出
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void asyncExport() {

        // 从队列中获取一个任务
        QueueInfo queueInfo  = (QueueInfo) redisUtil.lGet(RedisConstant.FILE_EXPORT_ASYNC_LIST);

        if (Objects.isNull(queueInfo)) {
            return;
        }

        // 导出文件
        exportStrategy.doAsyncExport(queueInfo);
    }

    /**
     * 异步导出任务检查
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void asyncExportCheck() {

        List<PdfGeneratorVO> pdfGeneratorList = redisUtil.getListByKeyPrefix(RedisConstant.FILE_EXPORT_ASYNC_TASK_KEY);
        if (CollectionUtils.isEmpty(pdfGeneratorList)) {
            return;
        }
        for (PdfGeneratorVO pdfGenerator : pdfGeneratorList) {
            // 这里不处理异常的问题，只处理回调异常的
            Date createTime = pdfGenerator.getCreateTime();
            if (DateUtil.between(createTime, new Date(), DateUnit.MINUTE) <= 60) {
                return;
            }
            // 如果超过60分钟，则判定为回调异常，发送异常通知
            noticeService.sendErrorNotice(pdfGenerator.getExportUuid(), pdfGenerator);
        }
    }
}
