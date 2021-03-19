package com.wupol.myopia.business.management.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 筛查数据导出
 * @author Alix
 * @Date 2021/03/12
 **/
@Data
@Accessors(chain = true)
public class VisionScreeningResultReportVo implements Serializable {
    /** 序号 */
    private Integer id;

    /** 姓名 */
    private String studentName;

    /** 性别 */
    private String genderDesc;

    /** 戴镜情况 */
    private String glassesTypeDesc;

    /** 裸眼（右/左） */
    private String nakedVisions;

    /** 矫正（右/左） */
    private String correctedVisions;

    /** 球镜（右/左） */
    private String sphs;

    /** 柱镜（右/左） */
    private String cyls;

    /** 轴位（右/左） */
    private String axials;

    /** 等效球镜（右/左） */
    private String sphericalEquivalents;

    /** 预警级别, 预警级别 */
    private Integer lowVisionWarningLevel;

    /** 矫正分析 */
    private String correctionDesc;
}