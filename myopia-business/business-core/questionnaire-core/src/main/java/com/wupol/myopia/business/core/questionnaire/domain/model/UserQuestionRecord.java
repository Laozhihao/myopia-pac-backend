package com.wupol.myopia.business.core.questionnaire.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 用户答问卷记录表
 *
 * @Author Simple4H
 * @Date 2022-07-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("q_user_question_record")
public class UserQuestionRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 问题类型，如：radio（单选）、checkbox（多选）、input（填空）
     */
    private String userId;

    /**
     * 问题题目
     */
    private String questionnaireId;

    /**
     * 问题属性
     */
    private String planId;

    /**
     * 问题的答案选项
     */
    private String taskId;

    /**
     * 问题的序号
     */
    private String noticeId;

    /**
     * 状态 0-未开始 1-进行中 2-结束
     */
    private Integer status;

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
