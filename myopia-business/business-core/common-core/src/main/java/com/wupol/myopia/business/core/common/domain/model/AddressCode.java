package com.wupol.myopia.business.core.common.domain.model;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 省市区镇Code
 *
 * @author Simple4H
 */
@Getter
@Setter
@Accessors(chain = true)
public class AddressCode implements Serializable {

    /**
     * 省代码
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long provinceCode;

    /**
     * 市代码
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long cityCode;

    /**
     * 区代码
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long areaCode;

    /**
     * 镇/乡代码
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long townCode;


}
