package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

/**
 * 龋齿监测结果实体
 *
 * @author hang.yuan 2022/5/16 17:10
 */
@Data
public class DistrictSaprodontiaMonitorVO {

    /**
     * 说明
     */
    private SaprodontiaMonitorVariableVO saprodontiaMonitorVariableVO;

    /**
     * 龋齿监测 - 不同性别
     */
    private SaprodontiaSexVO saprodontiaSexVO;

    /**
     * 龋齿监测 - 不同学龄段
     */
    private SaprodontiaSchoolAgeVO saprodontiaSchoolAgeVO;
    /**
     * 龋齿监测 - 不同年龄段
     */
    private SaprodontiaAgeVO saprodontiaAgeVO;


}
