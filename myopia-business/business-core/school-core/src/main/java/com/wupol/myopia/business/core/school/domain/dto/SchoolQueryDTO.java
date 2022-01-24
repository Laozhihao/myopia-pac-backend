package com.wupol.myopia.business.core.school.domain.dto;

import com.wupol.myopia.business.core.school.domain.model.School;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 学校查询
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class SchoolQueryDTO extends School {
    /** id */
    private String noLike;
    /** 名称 */
    private String nameLike;
    /** 地区编码 */
    private Long code;

    /**
     * 创建人
     */
    private String createUser;
    /** 层级ID列表 **/
    private List<Integer> districtIds;
    /** 筛查机构ID **/
    private Integer screeningOrgId;
    /** 查询开始时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startTime;
    /** 查询结束时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endTime;
    /** 是否需要查询是否有计划（配合开始结束时间） **/
    private Boolean needCheckHavePlan;

    private String startTimes;
    
    private String endTimes;
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

    public Boolean getNeedCheckHavePlan() {
        return !Objects.isNull(needCheckHavePlan) && needCheckHavePlan;
    }
}
