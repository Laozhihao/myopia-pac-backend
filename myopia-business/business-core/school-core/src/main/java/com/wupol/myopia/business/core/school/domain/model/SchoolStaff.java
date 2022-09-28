package com.wupol.myopia.business.core.school.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.core.school.domain.dos.AccountInfo;
import com.wupol.myopia.business.core.school.domain.handle.AccountInfoHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 学校员工表
 *
 * @author Simple4H
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "m_school_staff", autoResultMap = true)
public class SchoolStaff implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 学校Id
     */
    private Integer schoolId;

    /**
     * 姓名
     */
    private String staffName;

    /**
     * 性别：0-男、1-女
     */
    private Integer gender;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 身份证
     */
    private String idCard;

    /**
     * 角色 0-校医
     */
    private Integer staffType;

    /**
     * 用户表信息
     */
    @TableField(typeHandler = AccountInfoHandler.class)
    private List<AccountInfo> accountInfo;

    /**
     * 状态：0-启用 1-禁止 2-删除
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

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
