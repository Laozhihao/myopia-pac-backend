package com.wupol.myopia.business.aggregation.export;

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
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

        // 是否与进行中的任务重复
        Object obj = redisUtil.get(RedisConstant.FILE_EXPORT_ING);
        if (Objects.nonNull(obj)) {
            QueueInfo queueInfo = (QueueInfo) obj;
            if (isSameExport(queueInfo, exportCondition, serviceName)) {
                throw new BusinessException("正在导出中，请勿重复导出");
            }
        }
        // 判断任务是否重复
        List<Object> dutyList = redisUtil.lGetAll(RedisConstant.FILE_EXPORT_LIST);
        if (!CollectionUtils.isEmpty(dutyList)) {
            dutyList.forEach(queueInfos -> {
                QueueInfo queueInfo = (QueueInfo) queueInfos;
                if (isSameExport(queueInfo, exportCondition, serviceName)) {
                    throw new BusinessException("正在导出中，请勿重复导出");
                }
            });
        }
        // 设置进队列
        redisUtil.lSet(RedisConstant.FILE_EXPORT_LIST, new QueueInfo(exportCondition, serviceName));
    }

    public ExportFileService getExportFileService(String serviceName) {
        ExportFileService exportFileService = exportFileServiceMap.get(serviceName);
        Assert.notNull(exportFileService, "没有找到对应导出文件service实例：" + serviceName);
        return exportFileService;
    }

    /**
     * 是否相同的导出
     *
     * @param queueInfo       导出类
     * @param exportCondition 导出条件
     * @param serviceName     服务名
     * @return 是否相同
     */
    private boolean isSameExport(QueueInfo queueInfo, ExportCondition exportCondition, String serviceName) {
        return Objects.equals(queueInfo.getExportCondition(), exportCondition)
                && serviceName.equals(queueInfo.getServiceName());
    }
}
