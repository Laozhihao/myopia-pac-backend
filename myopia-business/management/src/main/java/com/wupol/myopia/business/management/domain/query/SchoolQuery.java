package com.wupol.myopia.business.management.domain.query;


import com.wupol.myopia.business.management.domain.model.School;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 学校查询
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class SchoolQuery extends School {
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
    /** 查询开始时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startTime;
    /** 查询结束时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endTime;
    /** 是否需要查询是否有计划（配合开始结束时间） **/
    private Boolean needCheckHavePlan = false;
}
