package com.wupol.myopia.business.core.hospital.domain.dto;

import com.wupol.myopia.business.core.hospital.domain.model.PreschoolCheckRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
     * 检查时年龄
     */
    private String checkAge;

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
    private String receiveHospital;

}
