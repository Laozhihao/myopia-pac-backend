package com.wupol.myopia.business.management.domain.dto;

import lombok.Data;

/**
 * @Description 搜索复查结果使用
 * @Date 2021/1/29 14:34
 * @Author by Jacob
 */
@Data
public class RescreeningResultDTO {
    /**
     * 筛查机构id
     */
    private Integer depId;
    /**
     * 年级名
     */
    private String gradeName;
    /**
     * 学校id
     */
    private Integer schoolId;
    /**
     * 班级名
     */
    private String clazzName;
}
