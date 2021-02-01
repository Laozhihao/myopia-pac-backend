package com.wupol.myopia.business.management.domain.vo;

import com.wupol.myopia.business.management.domain.model.ScreeningTaskOrg;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 筛查任务机构
 * @Author Alix
 * @Date 2021/01/25
 **/

@Data
@Accessors(chain = true)
public class ScreeningTaskOrgVo extends ScreeningTaskOrg {
    /** 筛查机构名称 */
    private String name;
}