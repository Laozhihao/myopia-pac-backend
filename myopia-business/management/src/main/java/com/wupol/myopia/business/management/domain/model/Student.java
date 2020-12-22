package com.wupol.myopia.business.management.domain.model;

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
 * 学校-学生表
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_student")
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 根据规则创建ID
     */
    private Long studentNo;

    /**
     * 创建人ID
     */
    private Integer createUserId;

    /**
     * 学号
     */
    private Integer sno;

    /**
     * 班级id
     */
    private Integer gradeId;

    /**
     * 年级ID
     */
    private Integer classId;

    /**
     * 学生姓名
     */
    private String name;

    /**
     * 性别 1-男 2-女
     */
    private Boolean gender;

    /**
     * 出生日期
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date birthday;

    /**
     * 民族 0-汉族
     */
    private Integer nation;

    /**
     * 身份证号码
     */
    private String idCard;

    /**
     * 家长手机号码
     */
    private String parentPhone;

    /**
     * 家长公众号手机号码
     */
    private String mpParentPhone;

    /**
     * 省代码
     */
    private Integer provinceCode;

    /**
     * 市代码
     */
    private Integer cityCode;

    /**
     * 区代码
     */
    private Integer areaCode;

    /**
     * 镇/乡代码
     */
    private Integer townCode;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 当前情况
     */
    private String currentSituation;

    /**
     * 视力标签
     */
    private String labels;

    /**
     * 视力筛查次数
     */
    private Integer screeningCount;

    /**
     * 问卷数
     */
    private Integer questionnaireCount;

    /**
     * 最近筛选次数
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastScreeningTime;

    /**
     * 状态 0-启用 1-禁止 2-删除
     */
    private Integer status;

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
