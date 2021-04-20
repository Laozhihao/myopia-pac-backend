package com.wupol.myopia.business.management.domain.vo;

import com.wupol.myopia.business.management.domain.model.ScreeningTask;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 筛查任务Vo
 * @author Alix
 * @Date 2021/01/25
 **/

@Data
@Accessors(chain = true)
public class ScreeningTaskVo extends ScreeningTask {
    /** 行政区域名 */
    private String creatorName;
    /** 行政区域名称 */
    private String districtName;
    /** 部门名称 */
    private String govDeptName;
}