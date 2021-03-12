package com.wupol.myopia.business.hospital.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.wupol.myopia.business.management.domain.model.Student;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
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
public class ReportConclusion {
    
    /** 学生信息 */
    private Student student;
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
    public static class ReportInfo {
        private String no;
        private Integer glassesSituation;
        private String medicalContent;
        private Date createTime;
        private List<Integer> imageIdList;

    }

}
