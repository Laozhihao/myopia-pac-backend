package com.wupol.myopia.business.management.domain.vo.bigscreening;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Date 2021/3/7 20:47
 * @Author by Jacob
 */
@NoArgsConstructor
@Data
public class AgeDTO {


    /**
     * $03
     */
    /**
     * 0-3 : 12.47
     * 4-6 : 23.45
     * 7-9 : 7.35
     * 10-12 : 15.5
     * 13-15 : 2.8
     * 16-18 : 14.8
     * 18-20 : 10.3
     */

    private Double $03;
    /**
     * $46
     */
    private Double $46;
    /**
     * $79
     */
    private Double $79;
    /**
     * $1012
     */
    private Double $1012;
    /**
     * $1315
     */
    private Double $1315;
    /**
     * $1618
     */
    private Double $1618;
    /**
     * $1820
     */
    private Double $1820;
}
