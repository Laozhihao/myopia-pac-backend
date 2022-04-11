package com.wupol.myopia.migrate.domain.dos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author HaoHao
 * @Date 2022/3/31
 **/
@AllArgsConstructor
@Accessors(chain = true)
@Data
public class ScreeningOrgAndStaffDO {

    private String oldScreeningOrgId;

    private Integer screeningOrgId;

    private String screeningOrgName;

    private Integer districtId;

    private Integer screeningStaffUserId;

    private String screeningStaffName;
}
