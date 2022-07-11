package com.wupol.myopia.business.core.questionnaire.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 
 *
 * @Author Simple4H
 * @Date 2022-07-06
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("q_questionnaire")
public class Questionnaire implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 问卷标题
     */
    private String title;

    /**
     * 区域ID
     */
    private Integer districtId;

    /**
     * 年份，如：2022
     */
    private Integer year;

    /**
     * 父ID，没有上级为-1
     */
    private Integer pid;

    /**
     * 问卷状态 0-未开始 1-进行中 2-结束
     */
    private Integer status;

    /**
     * qes文件地址
     */
    private String qesUrl;

    /**
     * 页面json数据
     */
    private String pageJson;

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


}
