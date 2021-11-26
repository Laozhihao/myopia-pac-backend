package com.wupol.myopia.business.core.system.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * APP渠道表
 *
 * @Author HaoHao
 * @Date 2021-11-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_app_channel")
public class AppChannel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 中文名称
     */
    private String cnName;

    /**
     * 英文名称
     */
    private String enName;

    /**
     * 状态，0-启用（默认）、1-停用、2-删除
     */
    private Boolean status;


}
