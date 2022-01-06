package com.wupol.myopia.business.core.hospital.domain.dto;

import com.wupol.myopia.business.core.hospital.domain.model.PreschoolCheckRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Author wulizhou
 * @Date 2022/1/4 20:29
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class PreschoolCheckRecordDTO extends PreschoolCheckRecord {

    /**
     * 学生名称
     */
    private String studentName;

    /**
     * 性别
     */
    private String gender;

    /**
     * 生日
     */
    private Date birthDay;

    /**
     * 编号
     */
    private String recordNo;

    /**
     * 结论
     */
    private String conclusion;

    /**
     * 转诊结论
     */
    private String referralConclusion;

    /**
     * 就诊医院
     */
    private String hospitalName;

    /**
     * 医师
     */
    private String doctorsName;

    /**
     * 接诊机构
     */
    private String toHospital;

    /**
     * 未做专项检查
     */
    private String specialMedical;

    /**
     * 初筛异常项目
     */
    private String diseaseMedical;

    /**
     * 转诊状态[0 待就诊；1 已接诊]
     */
    private Integer referralStatus = 0;

}
