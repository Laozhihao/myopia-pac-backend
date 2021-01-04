package com.wupol.myopia.business.management.domain.query;


import com.wupol.myopia.business.management.domain.model.School;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学校查询
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SchoolQuery extends School {
    /** id */
    private String noLike;
    /** 名称 */
    private String nameLike;
    /** 地区编码 */
    private Long code;
}
