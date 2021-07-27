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

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 学号
     */
    private String sno;

    /**
     * 家长电话
     */
    private String parentPhone;
}
