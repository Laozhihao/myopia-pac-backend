package com.wupol.myopia.business.management.domain.model;

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
 * 某个地区层级最新统计的重点视力对象情况表
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_district_attentive_objects_statistic")
public class DistrictAttentiveObjectsStatistic implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 重点视力对象--所属的通知id
     */
    private Integer screeningNoticeId;

    /**
     * 重点视力对象--所属的任务id
     */
    private Integer screeningTaskId;

    /**
     * 重点视力对象--所属的地区id
     */
    private Integer districtId;

    /**
     * 重点视力对象--零级预警人数（默认0）
     */
    private Integer visionLabel0Numbers;
    /**
     * 重点视力对象--是否 合计  0=否 1=是
     */
    private Integer isTotal;
    /**
     * 重点视力对象--零级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    private Integer visionLabel0Ratio;
    /**
     * 重点视力对象--一级预警人数（默认0）
     */
    private Integer visionLabel1Numbers;

    /**
     * 重点视力对象--一级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    private Integer visionLabel1Ratio;

    /**
     * 重点视力对象--二级预警人数（默认0）
     */
    private Integer visionLabel2Numbers;

    /**
     * 重点视力对象--二级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    private Integer visionLabel2Ratio;

    /**
     * 重点视力对象--三级预警人数（默认0）
     */
    private Integer visionLabel3Numbers;

    /**
     * 重点视力对象--三级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    private Integer visionLabel3Ratio;

    /**
     * 重点视力对象--重点视力对象数量（默认0）
     */
    private Integer keyWarningNumbers;

    /**
     * 重点视力对象--学生总数
     */
    private Integer studentNumbers;

    /**
     * 重点视力对象--更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}
