package com.wupol.myopia.business.api.management.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author wulizhou
 * @Date 2021/6/8 17:27
 */
@Data
@Accessors(chain = true)
public class StatPersonnelDTO {

    private long planScreeningNum;
    private long actualScreeningNum;
    private long validFirstScreeningNum;

}
