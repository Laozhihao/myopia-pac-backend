package com.wupol.myopia.business.core.screening.flow.domain.dos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 学校-学生表
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Data
@Accessors(chain = true)
public class StudentDO {

    /**
     * id
     */
    private Integer id;

    /**
     * 学校编码
     */
    private String schoolNo;

    /**
     * 创建人ID
     */
    private Integer createUserId;

    /**
     * 学号
     */
    private String sno;

    /**
     * 年级ID
     */
    private Integer gradeId;

    /**
     * 学龄段
     */
    private Integer gradeType;

    /**
     * 班级id
     */
    private Integer classId;

    /**
     * 学生姓名
     */
    private String name;

    /**
     * 性别 0-男 1-女
     */
    private Integer gender;

    /**
     * 出生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
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
    private Long provinceCode;

    /**
     * 市代码
     */
    private Long cityCode;

    /**
     * 区代码
     */
    private Long areaCode;

    /**
     * 镇/乡代码
     */
    private Long townCode;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 头像
     */
    private Integer avatarFileId;

    /**
     * 当前情况
     */
    private String currentSituation;

    /**
     * 视力标签 0-零级、1-一级、2-二级、3-三级
     */
    private Integer visionLabel;

    /**
     * 最近筛选次数
     */
    private Date lastScreeningTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态 0-启用 1-禁止 2-删除
     */
    private Integer status;

    /**
     * 戴镜类型
     */
    private Integer glassesType;

    /**
     * 是否近视
     */
    private Boolean isMyopia;

    /**
     * 是否远视
     */
    private Boolean isHyperopia;

    /**
     * 是否散光
     */
    private Boolean isAstigmatism;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 视力筛查次数
     */
    private Integer screeningCount;

    /**
     * 问卷数
     */
    private Integer questionnaireCount;

    /**
     * 就诊次数
     */
    private Integer numOfVisits;

}
