package com.wupol.myopia.business.core.school.domain.dos;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * 简化版学校（注意：如无非常必要，不要增加字段，会影响相关接口性能）
 */
@Data
public class SimpleSchoolDO {

    /**
     * id
     */
    private Integer id;

    /**
     * 学校名称
     */
    private String name;

    /**
     * 是否已有计划
     */
    @TableField(exist = false)
    private Boolean alreadyHavePlan;
}
