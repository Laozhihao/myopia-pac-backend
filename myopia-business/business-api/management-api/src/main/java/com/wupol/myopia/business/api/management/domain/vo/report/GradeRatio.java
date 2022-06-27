package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

@Data
public class GradeRatio {
    /**
     * 年级
     */
    private String grade;
    /**
     * 占比
     */
    private String ratio;

}