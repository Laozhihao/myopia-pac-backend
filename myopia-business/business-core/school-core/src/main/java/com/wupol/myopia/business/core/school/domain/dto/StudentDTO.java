package com.wupol.myopia.business.core.school.domain.dto;

import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.school.domain.model.Student;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

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
     * 学校行政区域
     */
    private String schoolDistrictName;

    /**
     * 筛查编号
     */
    private Long screeningCode;

    /**
     * 筛查码
     */
    private List<Long> screeningCodes;

    /**
     * 生日信息
     */
    private String birthdayInfo;

    /**
     * 委会区域List
     */
    private List<District> committeeLists;

    /**
     * 学校行政区域ID
     */
    private Integer schoolDistrictId;

    /**
     * 片区类型：1好片、2中片、3差片
     */
    private Integer schoolAreaType;

    /**
     * 监测点类型：1城区、2郊县
     */
    private Integer schoolMonitorType;

    /**
     * 近视矫正
     */
    private Integer correction;

    /**
     * 视力低下描述
     */
    private String lowVisionDesc;

}
