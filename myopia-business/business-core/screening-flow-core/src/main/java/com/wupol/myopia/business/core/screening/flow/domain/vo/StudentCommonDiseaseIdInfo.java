package com.wupol.myopia.business.core.screening.flow.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author HaoHao
 * @Date 2022/4/15
 **/
@Accessors(chain = true)
@Data
public class StudentCommonDiseaseIdInfo {
    /**
     * 常见病ID
     */
    String commonDiseaseId;
    /**
     * 省名称
     */
    String provinceName;
    /**
     * 省编号，2位
     */
    String provinceCode;
    /**
     * 市名称
     */
    String cityName;
    /**
     * 市编号，2位
     */
    String cityCode;
    /**
     * 区/县名称
     */
    String areaName;
    /**
     * 区/县编号，2位
     */
    String areaCode;
    /**
     * 学校名称
     */
    String schoolName;
    /**
     * 学校编号，2位，如：01
     */
    String schoolCode;
    /**
     * 年级名称
     */
    String gradeName;
    /**
     * 年级编号，2位
     */
    String gradeCode;
    /**
     * 学生编号，4位，如：0001
     */
    String studentCode;

    /**
     * 片区类型，1-好片、2-中片、3-差片
     */
    Integer areaType;

    /**
     * 监测点，1-城区、2-郊县
     */
    Integer monitorType;
}
