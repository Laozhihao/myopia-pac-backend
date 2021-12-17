package com.wupol.myopia.business.core.hospital.domain.dos;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * ReportAndRecordVo
 *
 * @author Simple4H
 */
@Data
@Accessors(chain = true)
public class ReportAndRecordDO {

    private Integer hospitalId;

    private Integer reportId;

    private Date createTime;

    private Date updateTime;

    private String hospitalName;

    private Integer studentId;

    /**
     * 戴镜类型
     */
    private Integer glassesSituation;

    /**
     * 综合处方
     */
    private String medicalContent;

    /**
     * 医生Id
     */
    private Integer doctorId;

    /**
     * 医生名称
     */
    private String doctorName;

    /**
     * 图片url
     */
    private List<String> imageFileUrl;

    /**
     * 学生姓名
     */
    private String name;

    /**
     * 性别 -1未知 0-男 1-女
     */
    private Integer gender;
}
