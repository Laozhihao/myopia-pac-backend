package com.wupol.myopia.business.aggregation.export;

import com.alibaba.fastjson.JSON;
import com.amazonaws.services.simpleworkflow.flow.Suspendable;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.interfaces.ExportFileService;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.aggregation.export.pdf.domain.QueueInfo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Log4j2
@Service
public class ExportStrategy {

    @Autowired
    Map<String, ExportFileService> exportFileServiceMap = new ConcurrentHashMap<>(8);

    @Autowired
    private RedisUtil redisUtil;

    public void doExport(ExportCondition exportCondition, String serviceName) throws IOException {

        ExportFileService exportFileService = getExportFileService(serviceName);
        // 数据校验
        exportFileService.validateBeforeExport(exportCondition);
        // 尝试获锁
        if (!exportFileService.tryLock(exportFileService.getLockKey(exportCondition))) {
            throw new BusinessException("正在导出中，请勿重复导出");
        }
        // 设置进队列
        redisUtil.lSet(RedisConstant.FILE_EXPORT_LIST, new QueueInfo(exportCondition, serviceName));
    }
    /**
    * @Description: 导出excel
    * @Param: [exportCondition, serviceName]
    * @return: void
    * @Author: 钓猫的小鱼
    * @Date: 2021/12/31
    */
    public void doExcelExport(ExportCondition exportCondition, String serviceName) throws IOException {
        ExportFileService exportFileService = getExportFileService(serviceName);

        // 尝试获锁
        if (!exportFileService.tryLock(exportFileService.getLockKey(exportCondition))) {
            throw new BusinessException("正在导出中，请勿重复导出");
        }

        // 设置进队列
        redisUtil.lSet(RedisConstant.FILE_EXPORT_LIST, new QueueInfo(exportCondition, serviceName));
    }

    private ExportFileService getExportFileService(String serviceName) {
        ExportFileService exportFileService = exportFileServiceMap.get(serviceName);

        Assert.notNull(exportFileService, "没有找到对应导出文件service实例：" + serviceName);
        return exportFileService;
    }

    /**
     * 导出文件
     *
     * @param queueInfo 导出
     */
    public void exportFile(QueueInfo queueInfo) {
        ExportFileService exportFileService = getExportFileService(queueInfo.getServiceName());

        ExportCondition exportCondition = queueInfo.getExportCondition();
        exportFileService.export(exportCondition);
    }

    /**
     * 同步导出文件
     *
     * @param exportCondition 条件
     * @param serviceName     服务名称
     * @return File
     */
    public String syncExport(ExportCondition exportCondition, String serviceName) {
        ExportFileService exportFileService = getExportFileService(serviceName);
        return exportFileService.syncExport(exportCondition);
    }
}
