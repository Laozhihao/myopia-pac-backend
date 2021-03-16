package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.business.management.domain.model.Student;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 学生DTO
 *
 * @author Simple4H
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class StudentDTO extends Student {
    /**
     * 年级名称
     */
    private String gradeName;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 学校ID
     */
    private Integer schoolId;

    /**
     * 民族描述
     */
    private String nationDesc;

    /**
     * 性别描述
     */
    private String genderDesc;

    /**
     * 筛查二维码地址
     */
    private String qrCodeUrl;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * token
     */
    private String token;

    /**
     * 戴镜类型
     */
    private Integer glassesType;

    /**
     * 左眼视力
     */
    private BigDecimal visionL;

    /**
     * 右眼视力
     */
    private BigDecimal visionR;
}
