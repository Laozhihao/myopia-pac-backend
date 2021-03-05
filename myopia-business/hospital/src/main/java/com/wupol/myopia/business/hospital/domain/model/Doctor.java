package com.wupol.myopia.business.hospital.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 医院-医生
 * @author Chikong
 * @date 2021-02-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("h_doctor")
public class Doctor implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 启用 */
    public static final Integer STATUS_ENABLE = 0;
    /** 禁止 */
    public static final Integer STATUS_DISABLE = 1;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 性别：0-男、1-女 */
    private Integer gender;
    private String name;
    private Integer userId;
    /** 个人简介 */
    private String remark;
    /** 职称 */
    private String titleName;
    /** 医院id */
    private Integer hospitalId;
    /** 科室id */
    private Integer departmentId;
    /** 科室名称 */
    private String departmentName;
    /** 头像 */
    private Integer avatarFileId;
    /** 电子签名 */
    private Integer signFileId;
    /** 状态 0-启用 1-禁止 */
    private Integer status;
    /** 创建时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /** 更新时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;


  }
