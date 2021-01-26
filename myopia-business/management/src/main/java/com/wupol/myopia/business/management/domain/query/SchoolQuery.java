package com.wupol.myopia.business.management.domain.query;


import com.wupol.myopia.business.management.domain.model.School;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
}
