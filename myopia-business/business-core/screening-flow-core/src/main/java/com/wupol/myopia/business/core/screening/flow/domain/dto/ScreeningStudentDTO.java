package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.dos.StudentDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 学生DTO
 *
 * @author Simple4H
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ScreeningStudentDTO extends StudentDO {
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
     * 筛查计划Id
     */
    private Integer planId;

    /**
     * 筛查计划学生Id
     */
    private Integer planStudentId;

    /**
     * 学生年龄
     */
    private Integer age;

    /**
     * 学生编码
     */
    private Long screeningCode;

//    /**
//     * 是否佩戴眼镜情况
//     */
//    private String glassesType;

    /**
     * 裸视力 右/左
     */
    private String nakedVision;

    /**
     * 矫正 右/左
     */
    private String correctedVision;

    /**
     * 球镜 右/左
     */
    private String sph;

    /**
     * 柱镜 右/左
     */
    private String cyl;

    /**
     * 轴位 右/左
     */
    private String axial;
}
