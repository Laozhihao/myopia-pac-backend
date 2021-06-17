package com.wupol.myopia.business.api.management.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author wulizhou
 * @Date 2021/6/9 11:15
 */
@Data
@Accessors(chain = true)
public class StatSchoolPersonnelDTO extends  StatPersonnelDTO {

    private String name;

}
