package com.wupol.myopia.business.core.screening.flow.domain.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 学生档案卡基本信息
 *
 * @author Simple4H
 */
@Getter
@Setter
public class CardInfoVO {

    /**
     * 名字
     */
    private String name;

    /**
     * 生日
     */
    private Date birthday;

    /**
     * 身份证号码
     */
    private String idCard;

    /**
     * 性别 0-男 1-女
     */
    private Integer gender;

    /**
     * 省代名称
     */
    private String provinceName;

    /**
     * 市代名称
     */
    private String cityName;

    /**
     * 区代名称
     */
    private String areaName;

    /**
     * 镇/乡名称
     */
    private String townName;

    /**
     * 检查时间
     */
    private Date screeningDate;

    /**
     * 班级信息
     */
    private String className;

    /**
     * 年级信息
     */
    private String gradeName;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 行政区域名
     */
    private String districtName;


}
