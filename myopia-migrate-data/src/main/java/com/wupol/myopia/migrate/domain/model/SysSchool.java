package com.wupol.myopia.migrate.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 学校表
 *
 * @Author HaoHao
 * @Date 2022-03-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_school")
public class SysSchool implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "school_id", type = IdType.AUTO)
    private String schoolId;

    /**
     * 学校名称
     */
    private String name;

    /**
     * 学生人数
     */
    private Integer num;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String region;

    /**
     * 区域
     */
    private String city;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 性质
     */
    private String state;

    /**
     * 备注
     */
    private String description;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 省编码
     */
    private Integer provinceCode;

    /**
     * 区域编码
     */
    private Integer regionCode;

    /**
     * 市编码
     */
    private Integer cityCode;

    /**
     * 片区编码
     */
    private Integer areaCode;

    /**
     * 监测点编码
     */
    private Integer pointCode;

    /**
     * 学校编码
     */
    private Integer schoolCode;

}
