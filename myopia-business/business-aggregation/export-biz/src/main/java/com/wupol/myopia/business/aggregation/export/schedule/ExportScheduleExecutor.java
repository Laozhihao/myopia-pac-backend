package com.wupol.myopia.business.aggregation.export.schedule;

import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.interfaces.ExportFileService;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.aggregation.export.pdf.domain.QueueInfo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
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

    @Scheduled(cron = "0/5 * * * * ?")
    public void export() throws IOException {

        // 从队列中获取一个任务
        QueueInfo queueInfo = (QueueInfo) redisUtil.lGet(RedisConstant.FILE_EXPORT_LIST);
        if (Objects.isNull(queueInfo)) {
            return;
        }

        // 导出文件
        exportStrategy.exportFile(queueInfo);
    }
}
