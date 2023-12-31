package com.wupol.myopia.business.core.questionnaire.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.core.questionnaire.domain.dto.QuestionnaireInfoDTO;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 
 *
 * @Author Simple4H
 * @Date 2022-07-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "q_questionnaire", autoResultMap = true)
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
     * 类型
     */
    private Integer type;

    /**
     * 问卷状态 0-未开始 1-进行中 2-结束
     */
    private Integer status;

    /**
     * qes文件地址
     */
    private String qesId;

    /**
     * 页面json数据
     */
    @TableField(typeHandler = JacksonTypeHandler.class, updateStrategy = FieldStrategy.IGNORED)
    private List<QuestionnaireInfoDTO> pageJson;

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
