package com.wupol.myopia.business.hospital.domain.dos;

import lombok.Data;
import lombok.experimental.Accessors;


/**
 * 固化的结论数据
 *
 * @author Simple4H
 */
@Data
@Accessors(chain = true)
public class ReportConclusionDataDO {

    /**
     * 报告编号
     */
    private String reportSn;

    /**
     * 报告日期
     */
    private String reportDate;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 出生日期
     */
    private String birthday;

    /**
     * 性别
     */
    private String gender;

    /**
     * 就诊日期
     */
    private String recordDate;

    /**
     * 就诊医院
     */
    private String hospitalName;


}
