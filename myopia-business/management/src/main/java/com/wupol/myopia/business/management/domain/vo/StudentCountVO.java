package com.wupol.myopia.business.management.domain.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 学生统计
 *
 * @author Simple4H
 */
@Getter
@Setter
public class StudentCountVO {

    /**
     * 学校编号
     */
    private String schoolNo;

    /**
     * 学生统计
     */
    private Integer count;
}
