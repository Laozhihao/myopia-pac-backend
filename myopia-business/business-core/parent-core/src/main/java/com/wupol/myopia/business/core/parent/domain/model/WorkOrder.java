package com.wupol.myopia.business.core.parent.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.wupol.myopia.business.core.parent.domain.dos.StudentDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;

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
    /**
     * 用户查看工单状态(未读)
     */
    public static final int USER_VIEW_STATUS_UNREAD = 0;

    /**
     * 用户查看工单状态(已读)
     */
    public static final int USER_VIEW_STATUS_READ = 1;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 学生姓名
     */
    @NotNull(message = "学生姓名不能为空")
    private String name;

    /**
     * 性别 0-男 1-女
     */
    @NotNull(message = "学生性别不能为空")
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
    @NotNull(message = "出生日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    /**
     * 学校ID
     */
    @NotNull(message = "学校不能为空")
    private Integer schoolId;

    /**
     * 年级ID
     */
    @NotNull(message = "年级不能为空")
    private Integer gradeId;

    /**
     * 班级ID
     */
    @NotNull(message = "班级不能为空")
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
     * 用户查看工单处理状态（0未读，1已读）
     */
    private Integer viewStatus;

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
     * newData
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private StudentDO newData;

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
    @NotNull(message = "筛查日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date screeningBeginTime;

    /**
     * 筛查结束时间
     */
    @NotNull(message = "筛查日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date screeningEndTime;

    /**
     * 是否发送短信通知 0-否 1-是
     */
    private Boolean isNotice;

    /**
     * 修改筛查记录id
     */
    private Integer screeningId;



}
