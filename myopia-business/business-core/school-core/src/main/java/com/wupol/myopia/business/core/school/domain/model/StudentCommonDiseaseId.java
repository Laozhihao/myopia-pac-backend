package com.wupol.myopia.business.core.school.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 学生常见病ID
 *
 * @Author HaoHao
 * @Date 2022-05-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_student_common_disease_id")
public class StudentCommonDiseaseId implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 学生ID
     */
    private Integer studentId;

    /**
     * 区/县行政区域编码（6位）
     */
    private String areaDistrictShortCode;

    /**
     * 学校ID
     */
    private Integer schoolId;

    /**
     * 年级ID
     */
    private Integer gradeId;

    /**
     * 年份，如：2016、2019、2022
     */
    private Integer year;

    /**
     * 学生常见病编码，4位（同一年，同年级下，从0001到9999）
     */
    private String commonDiseaseCode;

    /**
     * 学生常见病ID，16位
     */
    private String commonDiseaseId;

    /**
     * 片区类型：1好片、2中片、3差片
     */
    private Integer areaType;

    /**
     * 监测点类型：1城区、2郊县
     */
    private Integer monitorType;

    /**
     * 创建时间
     */
    private Date createTime;

}
