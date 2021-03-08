package com.wupol.myopia.business.parent.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 获取就诊记录请求
 *
 * @author Simple4H
 */
@Getter
@Setter
public class VisitsReportDetailRequest {
    
    private Integer recordId;
    
    private Integer reportId;
}
