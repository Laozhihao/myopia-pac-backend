package com.wupol.myopia.business.management.domain.vo;

import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.domain.model.Student;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 政府部门, 显示
 * @Author Chikong
 * @Date 2020/12/22
 **/

@Data
@Accessors(chain = true)
public class GovDeptVo extends GovDept {
    /** 行政区域名 */
    private String districtName;

}