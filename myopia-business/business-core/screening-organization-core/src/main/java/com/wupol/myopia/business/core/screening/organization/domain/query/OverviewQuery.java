package com.wupol.myopia.business.core.screening.organization.domain.query;

import com.wupol.myopia.business.core.screening.organization.domain.model.Overview;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 总览账号查询
 * @Author wulizhou
 * @Date 2022/2/22 18:05
 */
@Getter
@Setter
@Accessors(chain = true)
public class OverviewQuery extends Overview {

    /** 名称 */
    private String nameLike;
    /** 联系人 */
    private String contactPersonLike;

    /**
     * 过期时间大于expireDayGt天
     */
    private Integer expireDayGt;
    /**
     * 过期时间小于等于expireDayLe天
     */
    private Integer expireDayLe;

    /**
     * 过期时间大于等于cooperationEndTimeGe
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date cooperationEndTimeGe;

    /**
     * 过期时间小于等于cooperationEndTimeLe
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date cooperationEndTimeLe;

}
