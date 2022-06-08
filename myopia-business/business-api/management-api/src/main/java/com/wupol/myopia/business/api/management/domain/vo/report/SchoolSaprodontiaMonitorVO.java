package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

import java.util.List;

/**
 * 龋齿监测结果
 *
 * @author hang.yuan 2022/5/16 17:10
 */
@Data
public class SchoolSaprodontiaMonitorVO {

    /**
     * 说明
     */
    private SaprodontiaMonitorVariableVO saprodontiaMonitorVariableVO;

    /**
     * 龋齿监测 - 不同性别
     */
    private SaprodontiaSexVO saprodontiaSexVO;

    /**
     * 龋齿监测 - 不同年级段
     */
    private SaprodontiaGradeVO saprodontiaGradeVO;
    /**
     * 龋齿监测 - 不同年龄
     */
    private SaprodontiaAgeVO saprodontiaAgeVO;







}
