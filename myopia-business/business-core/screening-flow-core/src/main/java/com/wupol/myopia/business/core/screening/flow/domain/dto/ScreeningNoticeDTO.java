package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 筛查通知
 * @Author Alix
 * @Date 2021/01/25
 **/

@Data
@Accessors(chain = true)
public class ScreeningNoticeDTO extends ScreeningNotice {
    /** 行政区域名 */
    private String creatorName;
    /** 筛查通知--接收通知对象的id（机构id 或者 部门id）*/
    private Integer acceptOrgId;
    /** 筛查通知--对应的screeningNoticeDeptOrgId*/
    private Integer screeningNoticeDeptOrgId;
    /** 筛查通知--操作状态（（0未发布 1 已发布 2 未创建 3已创建）*/
    private Integer operationStatus;
    /** 行政区域名称 */
    private String districtName;
    /** 部门名称 */
    private String govDeptName;
    /** 筛查通知--该通知对应的筛查任务或筛查计划ID */
    private Integer screeningTaskPlanId;
    /**
     * 是否为自己创建（0否 ，1是）
     */
    private Integer isSelfRelease;
    /**
     * 是否为发布的通知还是自己接受的通知(0发布的通知，1接受的通知)
     */
    private Integer isSelfReceive;
}