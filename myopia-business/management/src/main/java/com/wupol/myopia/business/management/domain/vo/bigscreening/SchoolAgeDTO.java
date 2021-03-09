package com.wupol.myopia.business.management.domain.vo.bigscreening;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Date 2021/3/7 20:47
 * @Author by Jacob
 */
@Data
public class SchoolAgeDTO {

    private Double kindergarten;
    /**
     * primary
     */
    private Double primary;
    /**
     * junior
     */
    private Double junior;
    /**
     * high
     */
    private Double high;
    /**
     * vocationalHigh
     */
    private Double vocationalHigh;
    /**
     * university
     */
    private Double university;
}
