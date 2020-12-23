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
 * 学校表
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("school")
public class School implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 根据规则创建ID
     */
    private Long schoolNo;

    /**
     * 创建人ID
     */
    private Integer createUserId;

    /**
     * 部门id
     */
    private Integer govDeptId;

    /**
     * 学校名称
     */
    private String name;

    /**
     * 学校性质 0-公办 1-私办 2-其他
     */
    private Integer kind;

    /**
     * 学校性质描述 0-公办 1-私办 2-其他
     */
    private String kindDesc;

    /**
     * 寄宿状态 0-全部住校 1-部分住校 2-不住校
     */
    private Integer lodgeStatus;

    /**
     * 学校类型 0-小学,1-初级中学,2-高级中学,3-完全中学,4-九年一贯制学校,5-十二年一贯制学校,6-职业高中,7其他
     */
    private Integer type;

    /**
     * 在校总人数
     */
    private Integer totalOnline;

    /**
     * 在校-男生人数
     */
    private Integer totalOnlineMale;

    /**
     * 在校-女生人数
     */
    private Integer totalOnlineFemale;

    /**
     * 住校总人数
     */
    private Integer totalLodge;

    /**
     * 住校-男生人数
     */
    private Integer totalLodgeMale;

    /**
     * 住校-女生人数
     */
    private Integer totalLodgeFemale;

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
     * 说明
     */
    private String remark;

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
