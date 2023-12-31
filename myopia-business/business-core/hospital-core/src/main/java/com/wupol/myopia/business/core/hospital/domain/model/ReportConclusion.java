package com.wupol.myopia.business.core.hospital.domain.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 报告的固化数据
 * @author Chikong
 * @date 2021-03-10
 */
@Getter
@Setter
@Accessors(chain = true)
public class ReportConclusion implements Serializable {
    
    /** 学生信息 */
    private HospitalStudent student;
    /** 医院名称 */
    private String hospitalName;
    /** 医生签名id */
    private Integer signFileId;
    /** 报告 */
    private ReportInfo report;
    /**
     * 问诊内容
     */
    private Consultation consultation;


    @Getter
    @Setter
    @Accessors(chain = true)
    public static class ReportInfo implements Serializable {
        private String no;
        private Integer glassesSituation;
        private String medicalContent;
        private Date createTime;
        private List<Integer> imageIdList;
    }

}
