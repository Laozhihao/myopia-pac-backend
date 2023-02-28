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
 * 
 *
 * @Author HaoHao
 * @Date 2022-03-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_grade_clazz")
public class SysGradeClass implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    /**
     * (0小班，1中班，2大班，3一年级，4二年级，5三年级。。。。)
     */
    private Integer sort;

    /**
     * 学校id
     */
    private String schoolId;

    /**
     * 年级
     */
    private String grade;

    /**
     * 班级
     */
    private String clazz;

    /**
     * 班级人数
     */
    private Long clazzNum;

    /**
     * 筛查人数
     */
    private Long screenNum;

    /**
     * 近视人数
     */
    private Long myopiaNum;

    /**
     * 近视率
     */
    private String myopiaRatio;

    /**
     * 轻度视力不良
     */
    private Long mildPoorEyesight;

    /**
     * 视力正常
     */
    private Long normalPoorEyesight;

    /**
     * 中度视力不良
     */
    private Long moderatePoorEyesight;

    /**
     * 重度视力不良
     */
    private Long severePoorEyesight;

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

}
