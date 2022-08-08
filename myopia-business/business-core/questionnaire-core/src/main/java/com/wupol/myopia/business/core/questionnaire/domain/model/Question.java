package com.wupol.myopia.business.core.questionnaire.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.core.questionnaire.domain.dos.Option;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QuestionAttribute;
import com.wupol.myopia.business.core.questionnaire.domain.handle.OptionHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Author Simple4H
 * @Date 2022-07-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "q_question", autoResultMap = true)
public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 顶层父级的标志Id
     */
    public static final int TOP_PARENT_ID = -1;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 问题类型，如：radio（单选）、checkbox（多选）、input（填空）
     */
    private String type;

    /**
     * 问题题目
     */
    private String title;

    /**
     * 副标题
     */
    private String subTitle;

    /**
     * 问题属性
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private QuestionAttribute attribute;

    /**
     * 问题的答案选项
     */
    @TableField(typeHandler = OptionHandler.class)
    private List<Option> options;

    /**
     * 问题的序号
     */
    private String serialNumber;

    /**
     * 父题目Id，没有父题目的则为-1
     */
    private Integer pid;

    /**
     * 相同问题uuid
     */
    private String sameQuestionGroupId;

    /**
     * 图标描述-前端需要
     */
    private String iconName;

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
