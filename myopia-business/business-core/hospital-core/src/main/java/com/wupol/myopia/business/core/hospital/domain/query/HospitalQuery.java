package com.wupol.myopia.business.core.hospital.domain.query;


import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.Objects;

/**
 * 医院查询
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HospitalQuery extends Hospital {
    /** 名称 */
    private String nameLike;
    /** 编号 */
    private String noLike;
    /** 地区编码 */
    private Long code;

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

    /**
     * 整合条件，计算出结束时间比较的>=时间
     * @return
     */
    public Date getCooperationEndGe() {
        if (Objects.nonNull(expireDayGt) && expireDayGt >= 0) {
            // 大于第n天，相当于大于等于第n+1天的第一个时刻
            Date expireDay = DateUtil.getStartTime(DateUtils.addDays(new Date(), expireDayGt + 1));
            // 当同时设定两个条件时，取最晚的时间
            return Objects.isNull(cooperationEndTimeGe) || expireDay.after(cooperationEndTimeGe) ? expireDay : cooperationEndTimeGe;
        }
        return cooperationEndTimeGe;
    }

    /**
     * 整合条件，计算出结束时间比较的<=时间
     * @return
     */
    public Date getCooperationEndle() {
        if (Objects.nonNull(expireDayLe) && expireDayLe >= 0) {
            // 小于等于第n天，即小于等于第n天的最后一个时刻
            Date expireDay = DateUtil.getEndTime(DateUtils.addDays(new Date(), expireDayLe));
            // 当同时设定两个条件时，取最早的时间
            return Objects.isNull(cooperationEndTimeLe) || expireDay.before(cooperationEndTimeLe) ? expireDay : cooperationEndTimeLe;
        }
        return cooperationEndTimeLe;
    }

}
