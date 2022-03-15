package com.wupol.myopia.business.core.parent.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.wupol.myopia.business.core.parent.domain.dos.StudentDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 工单实体
 * @Author xjl
 * @Date 2022-03-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_work_order")
public class WorkOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 学生姓名
     */
    private String name;

    /**
     * 性别 0-男 1-女
     */
    private Integer gender;

    /**
     * 护照
     */
    private String passport;

    /**
     * 身份证号码
     */
    private String idCard;

    /**
     * 出生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    /**
     * 学校ID
     */
    private Integer schoolId;

    /**
     * 年级ID
     */
    private Integer gradeId;

    /**
     * 班级ID
     */
    private Integer classId;

    /**
     * 学号
     */
    private String sno;

    /**
     * 状态 0-已处理 1-未处理 2-无法处理
     */
    private Integer status;

    /**
     * 创建人ID
     */
    private Integer createUserId;

    /**
     * 提交页面 0-绑定页面 1-档案页面
     */
    private Integer term;

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

    /**
     * oldData
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private StudentDO oldData;

    /**
     * 留言内容
     */
    private String content;

    /**
     * 学龄段
     */
    private Integer gradeType;

    /**
     * 家长手机号码
     */
    private String parentPhone;

    /**
     * 微信昵称
     */
    private String wxNickname;

    /**
     * 筛查开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date screeningBeginTime;

    /**
     * 筛查结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date screeningEndTime;

    /**
     * 是否发送短信通知 0-否 1-是
     */
    private Boolean isNotice;


}
