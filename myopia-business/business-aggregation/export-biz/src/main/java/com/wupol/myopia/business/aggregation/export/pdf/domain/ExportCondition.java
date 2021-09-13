package com.wupol.myopia.business.aggregation.export.pdf.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Accessors(chain = true)
@Data
public class ExportCondition {
    /**
     * 筛查通知ID
     **/
    private Integer notificationId;
    /**
     * 行政区域ID
     **/
    private Integer districtId;
    /**
     * 筛查计划ID
     **/
    private Integer planId;
    /**
     * 筛查机构ID
     **/
    private Integer screeningOrgId;
    /**
     * 申请导出文件用户ID
     **/
    private Integer applyExportFileUserId;
    /**
     * 学校ID
     **/
    private Integer schoolId;
    /**
     * 年级ID
     **/
    private Integer gradeId;

    /**
     * 班级Id
     */
    private Integer classId;
}
