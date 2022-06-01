package com.wupol.myopia.business.core.school.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 学校常见病编码
 *
 * @Author HaoHao
 * @Date 2022-05-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_school_common_disease_code")
public class SchoolCommonDiseaseCode implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 区/县行政区域编码（6位）
     */
    private String areaDistrictShortCode;

    /**
     * 学校ID
     */
    private Integer schoolId;

    /**
     * 年份
     */
    private Integer year;

    /**
     * 学校常见病编码，2位，同年同区域下学校从01到99
     */
    private String code;

}
