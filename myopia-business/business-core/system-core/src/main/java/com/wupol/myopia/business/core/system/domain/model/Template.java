package com.wupol.myopia.business.core.system.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 模板表
 *
 * @author Simple4H
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_template")
public class Template {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 业务类型：1-视力筛查、2-常见病
     */
    private Integer biz;

    /**
     * 1-档案卡 2-筛查报告
     */
    private Byte type;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}