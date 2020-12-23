package com.wupol.myopia.oauth.domain.model;

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
 * 用户表
 *
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("o_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 机构组织ID（如政府部门ID、学校ID、医院ID）
     */
    private Integer orgId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 性别：0-男、1-女
     */
    private Boolean gender;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 身份证号码
     */
    private String idCard;

    /**
     * 用户名（账号）
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 是否领导：0-否、1-是
     */
    private Boolean isLeader;

    /**
     * 系统编号
     */
    private Boolean systemCode;

    /**
     * 创建人
     */
    private Integer createUserId;

    /**
     * 状态：0-启用 1-禁止 2-删除
     */
    private Boolean status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;


}
