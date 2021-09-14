package com.wupol.myopia.business.aggregation.export.pdf.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 导出类
 *
 * @author Simple4H
 */
@NoArgsConstructor
@Getter
@Setter
public class QueueInfo {

    private ExportCondition exportCondition;

    private String serviceName;

    public QueueInfo(ExportCondition exportCondition, String serviceName) {
        this.exportCondition = exportCondition;
        this.serviceName = serviceName;
    }
}
