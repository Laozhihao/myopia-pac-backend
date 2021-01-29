package com.wupol.myopia.business.management.domain.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Date 2021/1/29 14:34
 * @Author by Jacob
 */
@Data
public class RescreeningResultVO {
    /**
     * -1 代表
     * 1 代表
     * 0 代表
     * 2 代表
     */
    private Integer qualified;
    /**
     * 年级名
     */
    private String gradeName;
    /**
     * 学校id
     */
    private Integer schoolId;
    /**
     * 复查数量
     */
    private Integer reviewsCount;
    /**
     * 学校名
     */
    private String schoolName;
    /**
     * 班级名
     */
    private String clazzName;
}
