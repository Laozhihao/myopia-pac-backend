package com.wupol.myopia.base.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 年龄段
 *
 * @author Simple4H
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgeGeneration {

    private String desc;

    private Integer leftAge;

    private Integer rightAge;
}
