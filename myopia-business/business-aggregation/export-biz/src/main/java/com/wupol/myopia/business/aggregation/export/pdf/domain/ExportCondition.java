package com.wupol.myopia.business.aggregation.export.pdf.domain;

import com.wupol.myopia.business.common.utils.constant.ExportTypeConst;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

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
    /**
     * 筛查学生Ids
     */
    private String planStudentIds;
    /**
     *  VS666数据
     */
    private List<Integer> ids;
    /**
     * 类型ID
     */
    private Integer type;

    /**
     * 导出类型
     * {@link ExportTypeConst}
     */
    private Integer exportType;

    /**
     * 是否幼儿园
     */
    private Boolean isKindergarten;
}
