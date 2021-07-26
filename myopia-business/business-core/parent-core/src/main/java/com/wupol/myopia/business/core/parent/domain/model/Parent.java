package com.wupol.myopia.business.core.parent.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 家长表
 *
 * @Author HaoHao
 * @Date 2021-02-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_parent")
public class Parent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 家长ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 微信openId
     */
    private String openId;

    /**
     * openId的hash值
     */
    private String hashKey;

    /**
     * 微信头像
     */
    private String wxHeaderImgUrl;

    /**
     * 微信昵称
     */
    private String wxNickname;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @TableField(exist = false)
    private String phone;
}
