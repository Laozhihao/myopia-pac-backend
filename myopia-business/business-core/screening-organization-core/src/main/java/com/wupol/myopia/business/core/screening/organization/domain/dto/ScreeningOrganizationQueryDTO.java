package com.wupol.myopia.business.core.screening.organization.domain.dto;


import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 筛查机构查询
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ScreeningOrganizationQueryDTO extends ScreeningOrganization {
    /** 机构id */
    private String orgIdLike;
    /** 机构名 */
    private String nameLike;
    /** 地区编码 */
    private Long code;
    /** 查询开始时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startTime;
    /** 查询结束时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endTime;
    /** 是否需要查询是否有任务（配合开始结束时间） **/
    private Boolean needCheckHaveTask = false;

    private String startTimes;

    private String endTimes;
}
