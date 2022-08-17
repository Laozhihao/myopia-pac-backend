package com.wupol.myopia.business.api.management.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Author wulizhou
 * @Date 2021/6/9 11:17
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class StatSchoolAgePersonnelDTO extends StatPersonnelDTO {

    private String schoolAge;

}
